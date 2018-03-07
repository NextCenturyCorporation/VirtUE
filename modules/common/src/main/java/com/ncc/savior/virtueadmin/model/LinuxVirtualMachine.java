package com.ncc.savior.virtueadmin.model;

import java.util.Set;

import javax.persistence.Entity;

/**
 * Represents one running Linux VM.
 * 
 * @author clong
 *
 */
@Entity
public class LinuxVirtualMachine extends AbstractVirtualMachine {

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

}
