package com.nextcentury.savior.cifsproxy.model;

import java.util.Set;

import org.springframework.lang.NonNull;

/**
 * Represents a single file share.
 * 
 * @author clong
 *
 */
public class FileShare implements Comparable<FileShare> {

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
	private String server;
	private String path;
	private Set<SharePermissions> permissions;
	private ShareType type;

	/**
	 * 
	 * @param name
	 *                        share name
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
	public FileShare(@NonNull String name, @NonNull String server, @NonNull String path,
			@NonNull Set<SharePermissions> permissions, @NonNull ShareType type) {
		this.name = name;
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
}
