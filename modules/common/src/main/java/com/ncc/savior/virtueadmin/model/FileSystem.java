package com.ncc.savior.virtueadmin.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

/**
 * Application Data Transfer Object (DTO).
 *
 *
 */
@Entity
public class FileSystem {
	@Id
	private String id;
	private String name;
	private boolean status;

	public FileSystem(String id, String name, boolean status) {
		super();
		this.id = id;
		this.name = name;
		this.status = status;
	}

	/**
	 * Used for jackson deserialization
	 */
	protected FileSystem() {

	}

	public FileSystem(String templateId, FileSystem fileSys) {
		this.id = templateId;
		this.name = fileSys.getName();
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public boolean isEnabled() {
		return status;
	}

	// below setters used for jackson deserialization
	public void setId(String id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setEnabled(boolean newStatus) {
		this.status = newStatus;
	}

	@Override
	public String toString() {
		return "FileSystem [id=" + id + ", name=" + name + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FileSystem other = (FileSystem) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	public static final Comparator<? super FileSystem> CASE_INSENSITIVE_NAME_COMPARATOR = new CaseInsensitiveNameComparator();
	private static class CaseInsensitiveNameComparator implements Comparator<FileSystem> {
		@Override
		public int compare(FileSystem o1, FileSystem o2) {
			return String.CASE_INSENSITIVE_ORDER.compare(o1.getName(), o2.getName());
		}
	}
}
