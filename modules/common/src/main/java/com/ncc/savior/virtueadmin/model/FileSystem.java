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
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;

import com.ncc.savior.virtueadmin.model.PersonalizableFileSystem;

/**
 *
 */
@Entity
@Access(AccessType.PROPERTY)
public class FileSystem extends PersonalizableFileSystem {


	public FileSystem(String id, String name, String address, boolean enabled,
										boolean readPerm, boolean writePerm, boolean executePerm) {
		super(id, name, address, enabled, readPerm, writePerm, executePerm);
	}

	/**
	 * Used for jackson deserialization
	 */
 	public FileSystem() {
		super();
	}

	public FileSystem(String templateId, FileSystem fileSys) {
		super(templateId, fileSys);
	}

	@Override
	@Id
	@Column(name = "id")
	public String getId() {
		return id;
	}
}
