package com.nextcentury.savior.cifsproxy.model;

import java.util.Collection;

import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

public abstract class SambaService {
	private static final XLogger LOGGER = XLoggerFactory.getXLogger(SambaService.class);

	/**
	 * Characters disallowed in file share names. Control characters (0x00-0x1F) are
	 * also invalid. From https://msdn.microsoft.com/en-us/library/cc422525.aspx
	 */
	protected static final String INVALID_SHARE_NAME_CHARS = "\"\\/[]:|<>+=;,*?";
	/**
	 * Maximum length for the name of a file share. From
	 * https://msdn.microsoft.com/en-us/library/cc246567.aspx
	 */
	protected static final int MAX_SHARE_NAME_LENGTH = 80;
	
	protected String name;
	protected String virtueId;
	protected String server;
	protected String exportedName;

	/**
	 * Generate a suitable export name for the share. Per Microsoft specs, it must
	 * be at most {@link FileShare#MAX_SHARE_NAME_LENGTH} characters long, and may
	 * not contain any characters from {@link FileShare#INVALID_SHARE_NAME_CHARS}.
	 * 
	 * To ensure functionality with Samba, leading and trailing spaces will not be
	 * generated, either. (It's possible that would work, but Samba strips leading
	 * spaces from normal parameter values.)
	 * 
	 * It also must be different from any export names currently in use, where case
	 * is not significant.
	 * 
	 * @param existingNames
	 * 
	 * @param share
	 * @return
	 */
	protected static String createExportName(String shareName, Collection<String> existingNames) {
		LOGGER.entry(shareName, existingNames);
		String startingName = shareName.trim();
		StringBuilder exportName = new StringBuilder();
		// replace invalid characters
		int maxLength = Math.min(startingName.length(), FileShare.MAX_SHARE_NAME_LENGTH);
		for (int i = 0; i < maxLength; i++) {
			int c = startingName.codePointAt(i);
			char newChar;
			if (FileShare.INVALID_SHARE_NAME_CHARS.indexOf(c) != -1 || c <= 0x1F) {
				newChar = '_';
			} else {
				newChar = startingName.charAt(i);
			}
			exportName.append(newChar);
		}

		// ensure there are no duplicates
		int suffix = 1;
		int baseLength = exportName.length();
		while (existingNames.contains(exportName.toString().toLowerCase())) {
			suffix++;
			String suffixAsString = Integer.toString(suffix);
			// replace the end with the suffix
			int newBaseLength = Math.min(baseLength, FileShare.MAX_SHARE_NAME_LENGTH - suffixAsString.length());
			exportName.replace(newBaseLength, exportName.length(), suffixAsString);
		}

		LOGGER.exit(exportName.toString());
		return exportName.toString();
	}

	/**
	 * 
	 * @return name of the share
	 */
	public String getName() {
		return name;
	}

	/**
	 * 
	 * @return the name of the Virtue
	 */
	public String getVirtueId() {
		return virtueId;
	}

	/**
	 * 
	 * @return the server where this share lives
	 */
	public String getServer() {
		return server;
	}

	public String getExportedName() {
		return exportedName;
	}

	public void initExportedName(Collection<String> existingExportedNames) throws IllegalStateException {
		LOGGER.entry(existingExportedNames);
		if (exportedName != null && exportedName.length() != 0) {
			IllegalStateException e = new IllegalStateException(
					"cannot init already-set exportedName '" + exportedName + "'");
			LOGGER.throwing(e);
			throw e;
		}
		exportedName = createExportName(name, existingExportedNames);
		LOGGER.exit();
	}

}