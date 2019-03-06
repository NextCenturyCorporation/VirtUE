/**
 * 
 */
package com.nextcentury.savior.cifsproxy.services;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.stereotype.Service;

import com.nextcentury.savior.cifsproxy.BaseSecurityConfig;
import com.nextcentury.savior.cifsproxy.model.SambaService;

/**
 * Manages configuration files for Samba. Prevents multiple services from
 * simultaneous writes that overwrite each other.
 * 
 * @author clong
 *
 */
@Service
@PropertySources({ @PropertySource(BaseSecurityConfig.DEFAULT_CIFS_PROXY_SECURITY_PROPERTIES_CLASSPATH),
		@PropertySource(value = BaseSecurityConfig.DEFAULT_CIFS_PROXY_SECURITY_PROPERTIES_WORKING_DIR, ignoreResourceNotFound = true) })
public class SambaConfigManager {
	private static final XLogger LOGGER = XLoggerFactory.getXLogger(SambaConfigManager.class);

	/**
	 * Serializes modifications to the samba config tree.
	 *
	 * @see #sambaConfigDir
	 */
	protected static final Object SAMBA_CONFIG_TREE_LOCK = new Object();

	/**
	 * A regular expression for a valid character in a POSIX filename (for POSIX
	 * "fully portable filenames").
	 */
	private static final String POSIX_FILENAME_CHAR_REGEX = "[0-9A-Za-z._-]";

	/**
	 * Maximum length of a POSIX-compliant filename.
	 */
	private static final int POSIX_FILENAME_MAX_LEN = 14;

	/**
	 * The directory where the Samba config files live (e.g., smb.conf).
	 */
	@Value("${savior.cifsproxy.sambaConfigDir:/etc/samba}")
	protected String sambaConfigDir;

	/**
	 * The relative path under {@link #sambaConfigDir} where individual share config
	 * files live.
	 */
	@Value("${savior.cifsproxy.virtueSharesConfigDir:virtue-shares}")
	protected String virtueSharesConfigDir;

	/**
	 * The helper shell program that creates the "virtue-shares.conf" file used as
	 * part of the samba config.
	 */
	@Value("${savior.cifsproxy.sambaConfigHelper:make-virtue-shares.sh}")
	protected String SAMBA_CONFIG_HELPER;

	private Set<String> exportedServiceNames = new HashSet<>();

	/**
	 * Make our Samba config directory, if necessary.
	 * 
	 * @throws UncheckedIOException
	 *                                  if the directory doesn't exist and cannot be
	 *                                  created
	 * @see #sambaConfigDir
	 * @see #virtueSharesConfigDir
	 */
	@PostConstruct
	protected void createConfigDirectory() throws UncheckedIOException {
		File configDir = new File(sambaConfigDir, virtueSharesConfigDir);
		synchronized (SAMBA_CONFIG_TREE_LOCK) {
			if (!configDir.exists()) {
				LOGGER.trace("making directory " + configDir);
				if (!configDir.mkdirs()) {
					UncheckedIOException wse = new UncheckedIOException(
							"could not create mount directory: " + configDir, new IOException());
					LOGGER.throwing(wse);
					throw wse;
				}
			}
		}
	}

	/**
	 * Make sure a name is suitable as a file name. Nearly all *nix filesystems
	 * allow any character except '/' (and null), but our filenames are only used
	 * internally so we can afford to be conservative and go with POSIX compliance
	 * (see {@link #POSIX_FILENAME_CHAR_REGEX}).
	 * 
	 * @param name
	 *                 original name
	 * @return a version of <code>name</code> that is a suitable (POSIX) filename
	 */
	static String sanitizeFilename(String name) {
		LOGGER.entry(name);
		StringBuilder filename = new StringBuilder();
		Pattern charRegex = Pattern.compile(POSIX_FILENAME_CHAR_REGEX);
		int maxLen = Math.min(name.length(), POSIX_FILENAME_MAX_LEN);
		for (int i = 0; i < maxLen; i++) {
			char c = name.charAt(i);
			if (charRegex.matcher(String.valueOf(c)).matches()) {
				filename.append(c);
			} else {
				filename.append("_");
			}
		}
		LOGGER.exit(filename.toString());
		return filename.toString();
	}

	/**
	 * Compute the config file for a service
	 * 
	 * @param name
	 *                     name of the service
	 * @param virtueId
	 *                     virtue to which it belongs
	 * @return where its config should be stored
	 */
	private File getSambaConfigFile(String name, String virtueId) {
		LOGGER.entry(name, virtueId);
		File configDir = new File(sambaConfigDir, virtueSharesConfigDir);
		File virtueConfigDir = new File(configDir, sanitizeFilename(virtueId));
		File configFile = new File(virtueConfigDir, sanitizeFilename(name) + ".conf");
		LOGGER.exit(configFile);
		return configFile;
	}

	/**
	 * Configure Samba to export the file share.
	 * 
	 * @param share
	 *                  file share to export
	 * @throws IOException
	 */
	void writeShareConfig(String name, String virtueId, String config) throws IOException {
		LOGGER.entry(name, virtueId, config);
		File configFile = getSambaConfigFile(name, virtueId);
		File virtueConfigDir = configFile.getParentFile();
		synchronized (SAMBA_CONFIG_TREE_LOCK) {
			LOGGER.debug("creating config directory '" + virtueConfigDir.getAbsolutePath() + "'");
			if (!virtueConfigDir.exists() && !virtueConfigDir.mkdirs()) {
				throw new IOException("could not create config dir: " + virtueConfigDir);
			}
			LOGGER.debug("writing config file '" + configFile.getAbsolutePath() + "'");
			try (FileWriter configWriter = new FileWriter(configFile)) {
				configWriter.write(config);
			}
		}
		updateSambaConfig();
		LOGGER.exit();
	}

	/**
	 * Update the master samba config to include the existing config files. This
	 * method will block if invoked simulaneously by more than one thread.
	 * 
	 * @throws IOException
	 *                         if there was a problem running the helper script
	 * @see #SAMBA_CONFIG_HELPER
	 */
	private void updateSambaConfig() throws IOException {
		LOGGER.entry();
		ProcessBuilder processBuilder = new ProcessBuilder(SAMBA_CONFIG_HELPER);
		processBuilder.directory(new File(sambaConfigDir));
		int retval;
		synchronized (SAMBA_CONFIG_TREE_LOCK) {
			Process process = processBuilder.start();
			try {
				retval = process.waitFor();
			} catch (InterruptedException e) {
				LOGGER.warn("Samba configuration helper was interrupted. Samba configuration may not be correct.");
				retval = 0;
			}
		}
		if (retval != 0) {
			IOException e = new IOException(
					"error result from Samba configuration helper '" + SAMBA_CONFIG_HELPER + "': " + retval);
			LOGGER.throwing(e);
			throw e;
		}
		LOGGER.exit();
	}

	/**
	 * Remove the config file associated with a service.
	 * 
	 * @param name
	 *                     service name
	 * @param virtueId
	 *                     virtue the service belongs to
	 * @throws IOException
	 *                         if there was an error updating the configuration
	 */
	void removeConfigFile(String name, String virtueId) throws IOException {
		File sambaConfigFile = getSambaConfigFile(name, virtueId);
		synchronized (SAMBA_CONFIG_TREE_LOCK) {
			Files.deleteIfExists(sambaConfigFile.toPath());
			// clean up the directory if it's empty
			File parent = sambaConfigFile.getParentFile();
			String[] siblings = parent.list();
			if (siblings.length == 0) {
				parent.delete();
			}
		}
		updateSambaConfig();
	}

	void initExportedName(SambaService service) {
		synchronized (exportedServiceNames) {
			service.initExportedName(exportedServiceNames);
			exportedServiceNames.add(service.getExportedName());
		}
	}

	void unregisterExportedName(String exportedName) {
		synchronized (exportedServiceNames) {
			exportedServiceNames.remove(exportedName);
		}
	}

}
