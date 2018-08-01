package com.ncc.savior.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * Property manager that aggregates multiple sources of properties with specific
 * Precedence to handle application configuration. <br>
 * <br>
 * Procedence order (first wins):
 * <ol>
 * <li>System properties
 * <li>Environmental properties
 * <li>Property files in order given in constructor (first wins)
 * </ol>
 */

public class PropertyManager {
	private static final String DEFAULT_PROPERTIES_PATH = "classpath:default.properties";
	private static final String SAVIOR_DEFAULT_PROPERTY_LOCATION = "file:./savior.properties";
	private static final Logger logger = LoggerFactory.getLogger(PropertyManager.class);
	public static final String PROPERTY_DESKTOP_API_PATH = "savior.api.path.desktop";
	public static final String PROPERTY_LOCATION_KEY = "savior.property.path";
	private static final String SAVIOR_DEFAULT_USER_PROPERTY_LOCATION = "file:./savior-user.properties";
	public static final String PROPERTY_REQUIRED_DOMAIN = "savior.domain";
	public static final String PROPERTY_DEFAULT_PEM = "savior.desktop.defaultPem";
	public static final String PROPERTY_ALLOW_INSECURE_SSL = "savior.desktop.allowInsecureSsl";
	public static final String PROPERTY_USE_COLORS = "savior.desktop.useColors";
	public static final String PROPERTY_STYLE = "savior.desktop.style";
	public static final String PROPERTY_LOGIN_API_PATH = "savior.api.path.login";
	public static final String PROPERTY_LOGOUT_API_PATH = "savior.api.path.logout";
	public static final String PROPERTY_BASE_API_PATH = "savior.api.path.base";
	public static final String PROPERTY_FREERDP_PATH = "savior.desktop.freerdp.path";
	public static final String PROPERTY_CLIPBOARD_JAR_PATH = "savior.desktop.clipboard.jar";
	private Properties props;

	public PropertyManager(File... files) {
		this(filesToStreams(files));
	}

	public PropertyManager(List<InputStream> iss) {
		InputStream is;
		props = new Properties();
		for (int i = iss.size() - 1; i >= 0; i--) {
			is = iss.get(i);
			try {
				props.load(is);
			} catch (IOException e) {
				logger.warn("Error loading properties.");
			}
		}
	}

	public PropertyManager(String... resourcePaths) {
		this(resourcePathsToStreams(resourcePaths));
	}

	private static List<InputStream> filesToStreams(File[] files) {
		ArrayList<InputStream> list = new ArrayList<InputStream>();
		for (File file : files) {
			if (file != null && file.exists() && file.isFile()) {
				try {
					list.add(new FileInputStream(file));
				} catch (FileNotFoundException e) {
					logger.warn("Error opening file at " + file);
				}
			} else {
				logger.warn("Unable to open file at " + file);
			}
		}
		return list;
	}

	private static List<InputStream> resourcePathsToStreams(String[] resourcePaths) {
		PathMatchingResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver();
		List<InputStream> list = new ArrayList<InputStream>();
		for (String path : resourcePaths) {
			try {
				if (path != null) {
					Resource resource = resourceResolver.getResource(path);
					InputStream is = resource.getInputStream();
					list.add(is);
				}
			} catch (IOException e) {
				logger.warn("Unable to open resource at " + path);
			}
		}
		return list;
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

	public static PropertyManager defaultPropertyLocations(boolean debugOutput) {
		String defaultsPath = null;

		defaultsPath = DEFAULT_PROPERTIES_PATH;

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
		PropertyManager props = new PropertyManager(extraPropsLocation, defaultUserLocation, defaultPropsLocation,
				defaultsPath);
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
