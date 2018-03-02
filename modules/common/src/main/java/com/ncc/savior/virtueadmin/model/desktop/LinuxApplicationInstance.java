package com.ncc.savior.virtueadmin.model.desktop;

import com.ncc.savior.virtueadmin.model.ApplicationDefinition;

public class LinuxApplicationInstance extends BaseApplicationInstance {
	private int port;
	private String userName;
	private String privateKey;
	private String hostname;

	protected LinuxApplicationInstance() {

	}

	public LinuxApplicationInstance(ApplicationDefinition application, String hostname, int sshPort, String userName,
			String privateKey) {		
		setApplicationDefinition(application);
		this.hostname = hostname;
		this.port = sshPort;
		this.userName = userName;
		this.privateKey = privateKey;
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
		return "DesktopVirtueApplication [application=" + getApplicationDefinition()
				+ ", hostname=" + hostname + ", port=" + port + ", userName=" + userName + "]";
	}
}
