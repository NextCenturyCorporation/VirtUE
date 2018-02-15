package com.ncc.savior.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Property manager that aggregates multiple sources of properties with specific
 * Precedence to handle application configuration. </br>
 * </br>
 * Procedence order (first wins):
 * <ol>
 * <li>System properties
 * <li>Environmental properties
 * <li>Property files in order given in constructor (first wins)
 */

public class PropertyManager {
	private static final String DEFAULT_PROPERTIES_PATH = "default.properties";
	private static final String SAVIOR_DEFAULT_PROPERTY_LOCATION = "./savior.properties";
	private static final Logger logger = LoggerFactory.getLogger(PropertyManager.class);
	public static final String PROPERTY_DESKTOP_API_PATH = "savior.desktop.api.path";
	public static final String PROPERTY_LOCATION_KEY = "savior.property.path";
	private static final String SAVIOR_DEFAULT_USER_PROPERTY_LOCATION = "./savior-user.properties";
	public static final String PROPERTY_REQUIRED_DOMAIN = "savior.domain";
	public static final String PROPERTY_DUMMY_AUTHORIZATION = "savior.desktop.security.dummy";
	public static final String PROPERTY_DEFAULT_PEM = "savior.desktop.defaultPem";
	public static final String PROPERTY_ALLOW_INSECURE_SSL = "savior.desktop.allowInsecureSsl";
	public static final String PROPERTY_USE_COLORS = "savior.desktop.useColors";
	public static final String PROPERTY_STYLE = "savior.desktop.style";
	public static final String PROPERTY_SWING = "savior.desktop.swing";
	private Properties props;
	private boolean warnOnMissingFile = false;

	public PropertyManager(String... filePaths) {
		this(false, stringsToFiles(filePaths));
	}

	public PropertyManager(File... files) {
		this(false, files);
	}

	public PropertyManager(boolean debugOutput, File... files) {
		warnOnMissingFile = debugOutput;
		File file;
		props = new Properties();
		for (int i = files.length - 1; i >= 0; i--) {
			file = files[i];
			if (file != null && file.exists() && !file.isDirectory()) {
				try {
					props.load(new FileInputStream(file));
				} catch (IOException e) {
					logger.warn("Error loading file at " + file.getAbsolutePath());
				}
			} else {
				if (warnOnMissingFile) {
					logger.warn("Could not find property file " + file.getAbsolutePath());
				}
			}
		}
	}

	public PropertyManager(boolean debugOutput, String... filePaths) {
		this(debugOutput, stringsToFiles(filePaths));
	}

	public String getString(String property, String defaultValue) {
		return getProperty(property, defaultValue);
	}

	public String getString(String property) {
		return getProperty(property, null);
	}

	private String getProperty(String property, String defaultValue) {
		String val = System.getProperty(property);
		if (val == null) {
			val = System.getenv(property);
		}
		if (val == null) {
			val = props.getProperty(property, defaultValue);
		}
		return val;
	}

	private static File[] stringsToFiles(String[] filePaths) {
		ArrayList<File> files = new ArrayList<File>();
		for (String filePath : filePaths) {
			if (filePath != null) {
				File file = new File(filePath);
				files.add(file);
			}
		}
		return files.toArray(new File[0]);
	}

	public void setWarnOnMissingFile(boolean warnOnMissingFile) {
		this.warnOnMissingFile = warnOnMissingFile;
	}

	public static PropertyManager defaultPropertyLocations(boolean debugOutput) {
		String defaultsPath = null;
		try {
			defaultsPath = PropertyManager.class.getClassLoader().getResource(DEFAULT_PROPERTIES_PATH).toURI()
					.getPath();
		} catch (URISyntaxException e) {
			logger.warn("Error attempting to find default.properties", e);
		}
		String systemLocation = System.getProperty(PROPERTY_LOCATION_KEY);
		String envLocation = System.getenv(PROPERTY_LOCATION_KEY);
		String extraPropsLocation = null;
		if (systemLocation != null) {
			extraPropsLocation = systemLocation;
		} else if (envLocation != null) {
			extraPropsLocation = envLocation;
		}
		String defaultPropsLocation = SAVIOR_DEFAULT_PROPERTY_LOCATION;
		String defaultUserLocation = SAVIOR_DEFAULT_USER_PROPERTY_LOCATION;
		;
		PropertyManager props = new PropertyManager(debugOutput, extraPropsLocation, defaultUserLocation,
				defaultPropsLocation, defaultsPath);
		return props;
	}

	public boolean getBoolean(String property, boolean defaultValue) {
		String strVal = getProperty(property, null);
		boolean value = false;
		if (strVal == null) {
			return defaultValue;
		}
		try {
			value = Boolean.valueOf(strVal);
		} catch (RuntimeException e) {
			value = defaultValue;
		}
		return value;
	}

}
