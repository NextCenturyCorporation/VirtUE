package com.ncc.savior.virtueadmin.model;

import java.util.Collection;

public class VirtualMachine {
	private String id;
	private String name;
	// app ID to application
	private Collection<ApplicationDefinition> applications;
	private OS os;
	private VmState state;
	private String hostname;
	private int sshPort;
	private String infrastructureId;

	public VirtualMachine(String id, String name, Collection<ApplicationDefinition> applications, VmState state, OS os,
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

	/**
	 * Used for Jackson deserialization.
	 */
	protected VirtualMachine() {

	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Collection<ApplicationDefinition> getApplications() {
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

	// Below setters are for jackson deserialization
	protected void setId(String id) {
		this.id = id;
	}

	protected void setName(String name) {
		this.name = name;
	}

	protected void setApplications(Collection<ApplicationDefinition> applications) {
		this.applications = applications;
	}

	protected void setOs(OS os) {
		this.os = os;
	}

	protected void setHostname(String hostname) {
		this.hostname = hostname;
	}

	protected void setSshPort(int sshPort) {
		this.sshPort = sshPort;
	}

	protected void setInfrastructureId(String infrastructureId) {
		this.infrastructureId = infrastructureId;
	}

	@Override
	public String toString() {
		return "VirtualMachine [id=" + id + ", name=" + name + ", applications=" + applications + ", os=" + os
				+ ", state=" + state + ", hostname=" + hostname + ", sshPort=" + sshPort + ", infrastructureId="
				+ infrastructureId + "]";
	}

	public ApplicationDefinition findApplicationById(String applicationId) {
		for(ApplicationDefinition app:applications) {
			if (app.getId().equals(applicationId)) {
				return app;
			}
		}
		return null;
	}
}
