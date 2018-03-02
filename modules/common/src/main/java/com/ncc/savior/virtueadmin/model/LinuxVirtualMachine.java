package com.ncc.savior.virtueadmin.model;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public class LinuxVirtualMachine extends AbstractVirtualMachine {

	private int sshPort;
	private String userName;
	@Column(length = 6000)
	private String privateKey;

	public LinuxVirtualMachine(String id, String name, Set<ApplicationDefinition> applications, VmState state,
			OS os, String instanceId, String publicDnsName, int sshPort, String sshLoginUsername, String privateKey,
			String publicIpAddress) {
		super(id, name, applications, state, os, instanceId, publicDnsName, publicIpAddress);
		this.sshPort = sshPort;
		userName = sshLoginUsername;
		this.privateKey = privateKey;		
	}
	
	protected LinuxVirtualMachine() {
	}

	public int getSshPort() {
		return sshPort;
	}

	public String getUserName() {
		return userName;
	}

	public String getPrivateKey() {
		return privateKey;
	}

	public void setPrivateKey(String privateKey) {
		this.privateKey = privateKey;
	}

	protected void setUserName(String userName) {
		this.userName = userName;
	}

	protected void setSshPort(int sshPort) {
		this.sshPort = sshPort;
	}

}
