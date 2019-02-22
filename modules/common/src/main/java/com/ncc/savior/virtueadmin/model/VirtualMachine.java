package com.ncc.savior.virtueadmin.model;

import java.util.ArrayList;
import java.util.Collection;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;

@Entity
@Schema(description = "Object to describe a virtual machine instance.")
public class VirtualMachine {
	@Id
	@Schema(description = "ID of the virtual machine.")
	private String id;
	@Schema(description = "Name of the virtual machine.")
	private String name;
	@Schema(description = "State of virtual machine.")
	private VmState state;
	@Schema(description = "Operating system that the VM is running.")
	private OS os;
	@Schema(description = "Hostname of virtual machine.  If public IPs are used by the system, this will be the AWS public hostname.")
	private String hostname;
	@Schema(description = "Port the server has SSH exposed on.")
	private int sshPort;
	@Schema(description = "ID used by the infrastructure system.  Typically set by AWS or Xen.")
	private String infrastructureId;
	@Schema(description = "Username to use to login to the virtual machine")
	private String userName;
	@Column(length = 6000)
	@Schema(description = "Private key to access the virtual machine via ssh.")
	private String privateKey;
	@Schema(description = "Name that maps to a private key stored via a key manager")
	private String privateKeyName;
	@Schema(description = "IP address of virtual machine.  If public IPs are used by the system, this will be the AWS public IP address.")
	private String ipAddress;
	// app ID to application
	@ManyToMany
	private Collection<ApplicationDefinition> applications;
	private String password;

	@Transient
	@Schema(description = "IDs for the applications that this virtual machine has available for the user.")
	private Collection<String> applicationIds;
	@Schema(description = "Hostname for the internal network that the virtual machine resides.")
	private String internalHostname;
	@Schema(description = "IP address for the internal network that the virtual machine resides.")
	private String privateIpAddress;
	private String windowsUser;

	public VirtualMachine(String id, String name, Collection<ApplicationDefinition> applications, VmState state, OS os,
			String infrastructureId, String hostname, int sshPort, String userName, String privateKey,
			String privateKeyName, String ipAddress) {
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
		this.ipAddress = ipAddress;
		this.privateKeyName = privateKeyName;

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

	@JsonIgnore
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

	public String getUserName() {
		return userName;
	}

	public String getPrivateKey() {
		return privateKey;
	}

	// Below setters are for jackson deserialization
	public void setId(String id) {
		this.id = id;
	}

	public void setPrivateKey(String privateKey) {
		this.privateKey = privateKey;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setApplications(Collection<ApplicationDefinition> applications) {
		this.applications = applications;
	}

	protected void setOs(OS os) {
		this.os = os;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public void setSshPort(int sshPort) {
		this.sshPort = sshPort;
	}

	public void setInfrastructureId(String infrastructureId) {
		this.infrastructureId = infrastructureId;
	}

	@Override
	public String toString() {
		return "VirtualMachine [id=" + id + ", name=" + name + ", state=" + state + ", os=" + os + ", hostname="
				+ hostname + ", sshPort=" + sshPort + ", infrastructureId=" + infrastructureId + ", userName="
				+ userName + ", privateKey=[protected], privateKeyName=" + privateKeyName + ", ipAddress=" + ipAddress
				+ ", applications=" + applications + ", applicationIds=" + applicationIds + ", internalHostname="
				+ internalHostname + "]";
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
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

	public String getPrivateKeyName() {
		return privateKeyName;
	}

	public void setPrivateKeyName(String privateKeyName) {
		this.privateKeyName = privateKeyName;
	}

	@JsonGetter
	public Collection<String> getApplicationIds() {
		if (applications != null) {
			applicationIds = new ArrayList<String>();
			for (ApplicationDefinition app : applications) {
				applicationIds.add(app.getId());
			}
		}
		return applicationIds;
	}

	protected void setApplicationIds(Collection<String> applicationIds) {
		this.applicationIds = applicationIds;
	}

	public void setInternalHostname(String hostname) {
		this.internalHostname = hostname;
	}

	public String getInternalHostname() {
		return this.internalHostname;
	}

	public void setInternalIpAddress(String privateIpAddress) {
		this.privateIpAddress = privateIpAddress;
	}

	public String getInternalIpAddress() {
		return this.privateIpAddress;
	}

	@JsonIgnore
	public String getPassword() {
		return password;
	}

	@JsonIgnore
	public void setPassword(String password) {
		this.password = password;
	}

	@JsonIgnore
	public void setWindowsUser(String user) {
		this.windowsUser = user;
	}

	@JsonIgnore
	public String getWindowsUser() {
		return windowsUser;
	}

}
