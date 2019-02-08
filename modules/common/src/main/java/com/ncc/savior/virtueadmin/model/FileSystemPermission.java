package com.ncc.savior.virtueadmin.model;

import java.util.Comparator;

import javax.persistence.Entity;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonGetter;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * This represents a specific virtue's filesystem permissions.
 */
 @Schema(description="Represents a virtue-unique set of permissions for an external file system, usually a windows share, which can be attached to the virtual machines of a virtue.")
public class FileSystemPermission {
	@Schema(description="ID of the given file system.")
	protected String fileSystemId;
	@Schema(description="The fileSystem metadata")
	protected FileSystem fileSystem;
	@Schema(description="Virtue-specific True/false value for read permission to the share.")
	protected boolean readPerm;
	@Schema(description="Virtue-specific True/false value for write permission to the share.")
	protected boolean writePerm;
	@Schema(description="Virtue-specific True/false value for execute permission to the share.")
	protected boolean executePerm;

	public FileSystemPermission(String fileSystemId, boolean readPerm, boolean writePerm, boolean executePerm) {
		super();
		this.fileSystemId = fileSystemId;
		this.readPerm = readPerm;
		this.writePerm = writePerm;
		this.executePerm = executePerm;
	}

	/**
	 * Used for jackson deserialization
	 */
 	public FileSystemPermission() {
		this.fileSystemId = "id_65536"; // easily searchable value, just for debugging
		this.readPerm = false;
		this.writePerm = true;
		this.executePerm = false;
	}

	public FileSystemPermission(FileSystem fileSys) {
		this.fileSystemId = fileSys.getId();
		this.fileSystem = fileSys;
		this.readPerm = fileSys.getReadPerm();
		this.writePerm = fileSys.getWritePerm();
		this.executePerm = fileSys.getExecutePerm();
	}

	public String getFileSystemId() {
		return fileSystemId;
	}

	public String getName() {
		return fileSystem.name;
	}

	public String getAddress() {
		return fileSystem.address;
	}

	public boolean isEnabled() {
		return fileSystem.enabled;
	}

	@JsonGetter
	public boolean getReadPerm() {
		return readPerm;
	}

	@JsonGetter
	public boolean getWritePerm() {
		return writePerm;
	}

	@JsonGetter
	public boolean getExecutePerm() {
		return executePerm;
	}

	// below setters used for jackson deserialization
	public void setFileSystemIdd(String fileSystemId) {
		this.fileSystemId = fileSystemId;
	}

	public void setReadPerm(boolean read) {
		this.readPerm = read;
	}

	public void setWritePerm(boolean write) {
		this.writePerm = write;
	}

	public void setExecutePerm(boolean execute) {
		this.executePerm = execute;
	}

	@Override
	public String toString() {
		return "FileSystemPermission [fileSystemId=" + fileSystemId + ", readPerm=" + readPerm + ", writePerm=" +
		 					writePerm + ", executePerm=" + executePerm + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fileSystemId == null) ? 0 : fileSystemId.hashCode());
		result = prime * result + (toInt(readPerm) ^ 2);
		result = prime * result + (toInt(writePerm) ^ 4);
		result = prime * result + (toInt(executePerm) ^ 6);
		return result;
	}

	private int toInt(Boolean val) {
		return val ? 1 : 0;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;

		if (getClass() != obj.getClass())
			return false;
		FileSystemPermission other = (FileSystemPermission) obj;

		// check if both null or same reference, and if not, then check equals.
		if (fileSystemId == null || !fileSystemId.equals(other.fileSystemId)) {
			return false;
		}

		return true;
	}
	public static final Comparator<? super FileSystemPermission> CASE_INSENSITIVE_NAME_COMPARATOR = new CaseInsensitiveNameComparator();
	protected static class CaseInsensitiveNameComparator implements Comparator<FileSystemPermission> {
		@Override
		public int compare(FileSystemPermission o1, FileSystemPermission o2) {
			return String.CASE_INSENSITIVE_ORDER.compare(o1.toString(), o2.toString());
		}
	}
}
