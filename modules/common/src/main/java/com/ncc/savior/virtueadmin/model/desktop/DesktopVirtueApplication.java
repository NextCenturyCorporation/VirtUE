package com.ncc.savior.virtueadmin.model.desktop;

import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.OS;

public class DesktopVirtueApplication {
	private String id;
	private String name;
	private String version;
	private OS os;
	private String hostname;
	private int port;
	private String userName;
	private String privateKey;

	public DesktopVirtueApplication(String id, String name, String version, OS os, String hostname, int port,
			String userName, String privateKey) {
		super();
		this.id = id;
		this.name = name;
		this.version = version;
		this.os = os;
		this.hostname = hostname;
		this.port = port;
		this.userName = userName;
		this.privateKey = privateKey;
	}

	protected DesktopVirtueApplication() {

	}

	public DesktopVirtueApplication(ApplicationDefinition application, String hostname, int sshPort, String userName,
			String privateKey) {
		this(application.getId(), application.getName(), application.getVersion(), application.getOs(), hostname,
				sshPort, userName, privateKey);
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

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPrivateKey() {
		return privateKey;
	}

	public void setPrivateKey(String privateKey) {
		this.privateKey = privateKey;
	}

	@Override
	public String toString() {
		return "DesktopVirtueApplication [id=" + id + ", name=" + name + ", version=" + version + ", os=" + os
				+ ", hostname=" + hostname + ", port=" + port + ", userName=" + userName + ", privateKey=" + privateKey
				+ "]";
	}
}
