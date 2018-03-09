package com.ncc.savior.virtueadmin.model;

import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Application Data Transfer Object (DTO).
 * 
 *
 */
@Entity
public class ApplicationDefinition {
	@Id
	private String id;
	private String name;
	private String version;
	private OS os;
	private String launchCommand;

	public ApplicationDefinition(String id, String displayName, String version, OS os, String launchCommand) {
		this.id = id;
		this.name = displayName;
		this.version = version;
		this.os = os;
		this.launchCommand = launchCommand;
	}

	/**
	 * As {@link #ApplicationDefinition(String, String, String, OS, String)}, but
	 * supplies a randomly-chosen ID.
	 * 
	 * @param displayName
	 *            application name
	 * @param version
	 *            version
	 * @param os
	 *            OS
	 * @param launchCommand
	 *            command to run
	 * @see UUID#randomUUID()
	 */
	public ApplicationDefinition(String displayName, String version, OS os, String launchCommand) {
		this(UUID.randomUUID().toString(), displayName, version, os, launchCommand);
	}
	
	/**
	 * Used for jackson deserialization
	 */
	protected ApplicationDefinition() {

	}

	public ApplicationDefinition(String templateId, ApplicationDefinition appDef) {
		this(templateId, appDef.getName(), appDef.getVersion(), appDef.getOs(), appDef.getLaunchCommand());
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

	public void setName(String name) {
		this.name = name;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public void setOs(OS os) {
		this.os = os;
	}

	public void setLaunchCommand(String launchCommand) {
		this.launchCommand = launchCommand;
	}

	@Override
	public String toString() {
		return "ApplicationDefinition [id=" + id + ", name=" + name + ", version=" + version + ", os=" + os
				+ ", launchCommand=" + launchCommand + "]";
	}
}
