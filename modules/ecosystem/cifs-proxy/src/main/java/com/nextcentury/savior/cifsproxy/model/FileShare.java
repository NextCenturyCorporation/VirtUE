package com.nextcentury.savior.cifsproxy.model;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.springframework.lang.NonNull;

/**
 * Represents a single file share.
 * 
 * @author clong
 *
 */
public class FileShare implements Comparable<FileShare>, Exportable {

	private static final XLogger LOGGER = XLoggerFactory.getXLogger(FileShare.class);

	/**
	 * In the future we may support other types.
	 * 
	 * @author clong
	 *
	 */
	public enum ShareType {
		CIFS
	};

	/**
	 * Simple permission model for now, just read and write.
	 * 
	 * @author clong
	 *
	 */
	public enum SharePermissions {
		READ, WRITE
	}

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

	private String name;
	private String virtueId;
	private String server;
	private String path;
	private Set<SharePermissions> permissions;
	private ShareType type;
	private String exportedName;

	/**
	 * 
	 * @param name
	 *                        share name
	 * @param virtueId
	 *                        name of the Virtue this share belongs to
	 * @param server
	 *                        name of the file server. Can be a host name or IP
	 *                        address.
	 * @param path
	 *                        path to share on the server (e.g., "/public")
	 * @param permissions
	 *                        permissions to grant on the share
	 * @param type
	 *                        type of share
	 */
	public FileShare(@NonNull String name, @NonNull String virtueId, @NonNull String server, @NonNull String path,
			@NonNull Set<SharePermissions> permissions, @NonNull ShareType type) {
		this.name = name;
		this.virtueId = virtueId;
		this.server = server;
		this.path = path;
		this.permissions = permissions;
		this.type = type;
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

	/**
	 * 
	 * @return path to the share on the server (e.g., "public")
	 */
	public String getPath() {
		return path;
	}

	/**
	 * 
	 * @return permissions granted on the share
	 */
	public Set<SharePermissions> getPermissions() {
		return permissions;
	}

	/**
	 * 
	 * @return type of share
	 */
	public ShareType getType() {
		return type;
	}

	@Override
	public boolean equals(Object other) {
		if (other == null || !(other instanceof FileShare)) {
			return false;
		}
		FileShare fs = (FileShare) other;
		return name.equals(fs.name);
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public int compareTo(FileShare o) {
		return name.compareTo(o.getName());
	}

	@Override
	public String toString() {
		return "name: " + name + ", path: " + path + ", permissions: " + permissions + ", server: " + server
				+ ", type: " + type;
	}

	/* (non-Javadoc)
	 * @see com.nextcentury.savior.cifsproxy.model.Exportable#getExportedName()
	 */
	@Override
	public String getExportedName() {
		return exportedName;
	}

	public void initExportedName(Collection<? extends FileShare> existingFileShares) throws IllegalStateException {
		if (exportedName != null && exportedName.length() != 0) {
			IllegalStateException e = new IllegalStateException(
					"cannot init already-set exportedName '" + exportedName + "'");
			LOGGER.throwing(e);
			throw e;
		}
		exportedName = createExportName(name, existingFileShares);
	}

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
	 * @param existingFileShares
	 * 
	 * @param share
	 * @return
	 */
	protected static String createExportName(String shareName, Collection<? extends Exportable> existingFileShares) {
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
		List<String> existingNames = existingFileShares.stream().map(Exportable::getExportedName)
				.map(String::toLowerCase).collect(Collectors.toList());
		int suffix = 1;
		int baseLength = exportName.length();
		while (existingNames.contains(exportName.toString().toLowerCase())) {
			suffix++;
			String suffixAsString = Integer.toString(suffix);
			// replace the end with the suffix
			int newBaseLength = Math.min(baseLength, FileShare.MAX_SHARE_NAME_LENGTH - suffixAsString.length());
			exportName.replace(newBaseLength, exportName.length(), suffixAsString);
		}

		return exportName.toString();
	}
}
