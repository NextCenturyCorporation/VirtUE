package com.ncc.savior.virtueadmin.model;

/**
 * Application Data Transfer Object (DTO).
 * 
 *
 */
public class Application {
	private String id;
	private String name;
	private String version;
	private OS os;
	private String launchCommand;

	public Application(String id, String name, String version, OS os) {
		super();
		this.id = id;
		this.name = name;
		this.version = version;
		this.os = os;
	}

	public Application(String id, String displayName, String version, OS os, String launchCommand) {
		this.id=id;
		this.name=displayName;
		this.version=version;
		this.os=os;
		this.launchCommand=launchCommand;
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

}
