package com.ncc.savior.virtueadmin.model.desktop;

import com.ncc.savior.virtueadmin.model.OS;

public class DesktopVirtueApplication {
	private String id;
	private String name;
	private String version;
	private OS os;
	private String hostname;
	private int port;

	public DesktopVirtueApplication(String id, String name, String version, OS os, String hostname, int port) {
		super();
		this.id = id;
		this.name = name;
		this.version = version;
		this.os = os;
		this.hostname = hostname;
		this.port = port;
	}

	protected DesktopVirtueApplication() {
		
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public OS getOs() {
		return os;
	}

	public void setOs(OS os) {
		this.os = os;
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	@Override
	public String toString() {
		return "DesktopVirtueApplication [id=" + id + ", name=" + name + ", version=" + version + ", os=" + os
				+ ", hostname=" + hostname + ", port=" + port + "]";
	}
}
