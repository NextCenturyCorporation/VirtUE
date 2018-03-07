package com.ncc.savior.virtueadmin.model;

import java.util.Collection;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToMany;

@Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
public abstract class AbstractVirtualMachine {
	@Id
	private String id;
	private String name;
	private VmState state;
	private OS os;
	private String hostname;
	private String infrastructureId;
	private String ipAddress;
	// app ID to application
	@ManyToMany
	private Set<ApplicationDefinition> applications;
	protected int sshPort;
	protected String userName;
	@Column(length = 6000)
	protected String privateKey;
	
	protected AbstractVirtualMachine(String id, String name, Set<ApplicationDefinition> applications, VmState state, OS os,
			String infrastructureId, String hostname, String ipAddress) {
		super();
		this.id = id;
		this.name = name;
		this.applications = applications;
		this.state = state;
		this.os = os;
		this.infrastructureId = infrastructureId;
		this.hostname = hostname;
		this.ipAddress = ipAddress;
	}
	
	/**
	 * Used for Jackson deserialization.
	 */
	protected AbstractVirtualMachine() {

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

	protected void setApplications(Set<ApplicationDefinition> applications) {
		this.applications = applications;
	}

	protected void setOs(OS os) {
		this.os = os;
	}

	protected void setHostname(String hostname) {
		this.hostname = hostname;
	}

	protected void setInfrastructureId(String infrastructureId) {
		this.infrastructureId = infrastructureId;
	}

	@Override
	public String toString() {
		return "VirtualMachine [id=" + id + ", name=" + name + ", state=" + state + ", os=" + os + ", hostname="
				+ hostname + ", infrastructureId=" + infrastructureId + ", ipAddress=" + ipAddress + ", applications="
				+ applications + "]";
	}

	public String getIpAddress() {
		return ipAddress;
	}

	protected void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public ApplicationDefinition findApplicationById(String applicationId) {
		for (ApplicationDefinition app : applications) {
			if (app.getId().equals(applicationId)) {
				return app;
			}
		}
		return null;
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
