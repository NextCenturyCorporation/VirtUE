package com.ncc.savior.virtueadmin.model;

import java.security.cert.Certificate;

import javax.persistence.Entity;

@Entity
public class WindowsVirtualMachine extends AbstractVirtualMachine {

	private int rdpPort;
	private Certificate hostCert;
	private String userName;

	public WindowsVirtualMachine(int rdpPort, Certificate hostCert) {
		this.rdpPort = rdpPort;
		this.hostCert = hostCert;
	}

	protected WindowsVirtualMachine() {
	}

	public int getRdpPort() {
		return rdpPort;
	}

	protected void setRdpPort(int rdpPort) {
		this.rdpPort = rdpPort;
	}

	public Certificate getHostCert() {
		return hostCert;
	}

	protected void setHostCert(Certificate hostCert) {
		this.hostCert = hostCert;
	}

	public String getUserName() {
		return userName;
	}

	protected void setUserName(String userName) {
		this.userName = userName;
	}
}
