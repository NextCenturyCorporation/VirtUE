package com.nextcentury.savior.cifsproxy.model;

import java.util.Set;

import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.springframework.lang.NonNull;

/**
 * Represents a single file share.
 * 
 * @author clong
 *
 */
public class FileShare implements Comparable<FileShare> {

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

	public String getExportedName() {
		return exportedName;
	}
	
	public void initExportedName(String name) {
		if (exportedName != null && exportedName.length() != 0) {
			IllegalStateException e = new IllegalStateException("cannot init already-set exportedName '" + exportedName + "'");
			LOGGER.throwing(e);
			throw e;
		}
		exportedName = name;
	}
}
