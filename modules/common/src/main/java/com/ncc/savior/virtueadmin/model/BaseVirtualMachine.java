package com.ncc.savior.virtueadmin.model;

import javax.persistence.Column;
import javax.persistence.Id;

public abstract class BaseVirtualMachine implements HasId {
	@Id
	protected String id;
	protected String name;
	protected VmState state;
	protected OS os;
	protected String hostname;
	protected int sshPort;
	protected String infrastructureId;
	protected String userName;
	@Column(length = 6000)
	protected String privateKey;
	protected String privateKeyName;
	protected String ipAddress;

	protected BaseVirtualMachine(String id, String name, VmState state, OS os, String infrastructureId, String hostname,
			int sshPort, String userName, String privateKey, String privateKeyName, String ipAddress) {
		super();
		this.id = id;
		this.name = name;
		this.state = state;
		this.os = os;
		this.infrastructureId = infrastructureId;
		this.hostname = hostname;
		this.sshPort = sshPort;
		this.userName = userName;
		this.privateKey = privateKey;
		this.ipAddress = ipAddress;
		this.privateKeyName = privateKeyName;
	}

	/**
	 * Used for Jackson deserialization.
	 */
	protected BaseVirtualMachine() {

	}

	@Override
	public String getId() {
		return id;
	}

	public String getName() {
		return name;
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

	protected void setOs(OS os) {
		this.os = os;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	protected void setSshPort(int sshPort) {
		this.sshPort = sshPort;
	}

	protected void setInfrastructureId(String infrastructureId) {
		this.infrastructureId = infrastructureId;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String getPrivateKeyName() {
		return privateKeyName;
	}

	public void setPrivateKeyName(String privateKeyName) {
		this.privateKeyName = privateKeyName;
	}
}
