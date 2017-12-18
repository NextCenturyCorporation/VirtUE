package com.ncc.savior.virtueadmin.model;

import java.util.Map;

public class VirtualMachine {
	private String id;
	private String name;
	// app ID to application
	private Map<String, ApplicationDefinition> applications;
	private OS os;
	private VmState state;
	private String hostname;
	private int sshPort;
	private String infrastructureId;
	private String userName;
	private String privateKey;

	public VirtualMachine(String id, String name, Map<String, ApplicationDefinition> applications, VmState state, OS os,
			String infrastructureId, String hostname, int sshPort, String userName, String privateKey) {
		super();
		this.id = id;
		this.name = name;
		this.applications = applications;
		this.state = state;
		this.os = os;
		this.infrastructureId = infrastructureId;
		this.hostname = hostname;
		this.sshPort = sshPort;
		this.userName = userName;
		this.privateKey = privateKey;

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

	public Map<String, ApplicationDefinition> getApplications() {
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

	public String getUserName() {
		return userName;
	}

	public String getPrivateKey() {
		return privateKey;
	}

	// Below setters are for jackson deserialization
	protected void setId(String id) {
		this.id = id;
	}

	public void setPrivateKey(String privateKey) {
		this.privateKey = privateKey;
	}

	protected void setUserName(String userName) {
		this.userName = userName;
	}

	protected void setName(String name) {
		this.name = name;
	}

	protected void setApplications(Map<String, ApplicationDefinition> applications) {
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
}
