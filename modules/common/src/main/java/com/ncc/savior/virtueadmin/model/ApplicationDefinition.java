package com.ncc.savior.virtueadmin.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * Application Data Transfer Object (DTO).
 * 
 *
 */
@Entity
public class ApplicationDefinition {
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Id
	private String id;
	private String name;
	private String version;
	private OS os;
	private String launchCommand;

	public ApplicationDefinition(String id, String name, String version, OS os) {
		super();
		this.id = id;
		this.name = name;
		this.version = version;
		this.os = os;
	}

	public ApplicationDefinition(String id, String displayName, String version, OS os, String launchCommand) {
		this.id=id;
		this.name=displayName;
		this.version=version;
		this.os=os;
		this.launchCommand=launchCommand;
	}

	/**
	 * Used for jackson deserialization
	 */
	protected ApplicationDefinition() {

	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getVersion() {
		return version;
	}

	public OS getOs() {
		return os;
	}

	public String getLaunchCommand() {
		return launchCommand;
	}

	// below setters used for jackson deserialization
	protected void setId(String id) {
		this.id = id;
	}

	protected void setName(String name) {
		this.name = name;
	}

	protected void setVersion(String version) {
		this.version = version;
	}

	protected void setOs(OS os) {
		this.os = os;
	}

	protected void setLaunchCommand(String launchCommand) {
		this.launchCommand = launchCommand;
	}

}
