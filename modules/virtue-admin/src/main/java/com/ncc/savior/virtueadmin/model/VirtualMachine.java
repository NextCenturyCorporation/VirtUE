package com.ncc.savior.virtueadmin.model;

import java.util.Set;

public class VirtualMachine {
	private String id;
	private String name;
	private Set<ApplicationDefinition> applications;
	private OS os;
	private VmState state;
	private String hostname;
	private int sshPort;
	private String infrastructureId;

	public VirtualMachine(String id, String name, Set<ApplicationDefinition> applications, VmState state, OS os,
			String infrastructureId, String hostname, int sshPort) {
		super();
		this.id = id;
		this.name = name;
		this.applications = applications;
		this.state = state;
		this.os = os;
		this.infrastructureId = infrastructureId;
		this.hostname = hostname;
		this.sshPort = sshPort;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Set<ApplicationDefinition> getApplications() {
		return applications;
	}

	public VmState getState() {
		return state;
	}

	public String getHostname() {
		return hostname;
	}

	public OS getOs() {
		return os;
	}

	public int getSshPort() {
		return sshPort;
	}

	public String getInfrastructureId() {
		return infrastructureId;
	}

	public void setState(VmState state) {
		this.state = state;
	}

	@Override
	public String toString() {
		return "VirtualMachine [id=" + id + ", name=" + name + ", applications=" + applications + ", os=" + os
				+ ", state=" + state + ", hostname=" + hostname + ", sshPort=" + sshPort + ", infrastructureId="
				+ infrastructureId + "]";
	}
}
