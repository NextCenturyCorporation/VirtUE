package com.ncc.savior.virtueadmin.model;

import java.security.cert.Certificate;
import java.util.Set;

import javax.persistence.Entity;

@Entity
public class WindowsVirtualMachine extends AbstractVirtualMachine {

	private int rdpPort;
	private Certificate hostCert;

	public WindowsVirtualMachine(String id, String name, Set<ApplicationDefinition> apps, VmState state, OS os,
			String infrastructureId, String hostname, String ipAddress, int rdpPort) {
		super(id, name, apps, state, os, infrastructureId, hostname, ipAddress);
		this.rdpPort = rdpPort;
		this.hostCert = fetchHostCert();
	}

	private Certificate fetchHostCert() {
		// TODO Auto-generated method stub
		return null;
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
}
