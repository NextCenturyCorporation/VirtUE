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

	public Application(String id, String name, String version, OS os) {
		super();
		this.id = id;
		this.name = name;
		this.version = version;
		this.os = os;
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

}
