package com.ncc.savior.virtueadmin.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;
import javax.persistence.Embeddable;
import javax.persistence.MappedSuperclass;

/**
 *
 */
@Embeddable
@MappedSuperclass
public class PersonalizableFileSystem {
	protected String id;
	protected String address;
	protected String name;
	protected boolean enabled;
	protected boolean readPerm;
	protected boolean writePerm;
	protected boolean executePerm;

	public PersonalizableFileSystem(String id, String name, String address, boolean enabled,
										boolean readPerm, boolean writePerm, boolean executePerm) {
		super();
		this.id = id;
		this.address = address;
		this.name = name;
		this.enabled = enabled;
		this.readPerm = readPerm;
		this.writePerm = writePerm;
		this.executePerm = executePerm;
	}

	/**
	 * Used for jackson deserialization
	 */
 	public PersonalizableFileSystem() {
		this.id = "id_65536";
		this.name = "name_65536"; // easily searchable value, just for debugging
		this.address = "address_65536";
		this.enabled = true;
		this.readPerm = false;
		this.writePerm = true;
		this.executePerm = false;
	}

	public PersonalizableFileSystem(String templateId, PersonalizableFileSystem fileSys) {
		this.id = templateId;
		this.name = fileSys.getName();
		this.address = fileSys.getAddress();
		this.enabled = fileSys.isEnabled();
		this.readPerm = fileSys.getReadPerm();
		this.writePerm = fileSys.getWritePerm();
		this.executePerm = fileSys.getExecutePerm();
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getAddress() {
		return address;
	}

	@JsonGetter
	public boolean isEnabled() {
		return enabled;
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
	public void setId(String id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public void setEnabled(boolean newStatus) {
		this.enabled = newStatus;
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
		return "FileSystem [id=" + id + ", name=" + name + ", address=" + address + ", enabled=" + enabled +
		", readPerm=" + readPerm + ", writePerm=" + writePerm + ", executePerm=" + executePerm + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((address == null) ? 0 : address.hashCode());
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
		if (id == null || !id.equals(other.id))
			return false;
		if (name == null || !name.equals(other.name))
			return false;
		if (address == null || !address.equals(other.address))
			return false;

		return true;
	}
	public static final Comparator<? super FileSystem> CASE_INSENSITIVE_NAME_COMPARATOR = new CaseInsensitiveNameComparator();
	protected static class CaseInsensitiveNameComparator implements Comparator<FileSystem> {
		@Override
		public int compare(FileSystem o1, FileSystem o2) {
			return String.CASE_INSENSITIVE_ORDER.compare(o1.getName(), o2.getName());
		}
	}
}
