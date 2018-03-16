package com.ncc.savior.virtueadmin.model.persistance;

import java.util.Collection;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;

import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.BaseVirtualMachine;
import com.ncc.savior.virtueadmin.model.OS;
import com.ncc.savior.virtueadmin.model.VmState;

@Entity
public class JpaVirtualMachine extends BaseVirtualMachine {
	@ManyToMany
	private Collection<ApplicationDefinition> applications;

	public JpaVirtualMachine(String id, String name, Collection<ApplicationDefinition> applications, VmState state,
			OS os, String infrastructureId, String hostname, int sshPort, String userName, String privateKey,
			String privateKeyName, String ipAddress) {
		super(id, name, state, os, infrastructureId, hostname, sshPort, userName, privateKey, privateKeyName,
				ipAddress);
		this.applications = applications;
	}

	/**
	 * Used for Jackson deserialization.
	 */
	protected JpaVirtualMachine() {

	}

	public Collection<ApplicationDefinition> getApplications() {
		return applications;
	}

	protected void setApplications(Collection<ApplicationDefinition> applications) {
		this.applications = applications;
	}

	public ApplicationDefinition findApplicationById(String applicationId) {
		for (ApplicationDefinition app : applications) {
			if (app.getId().equals(applicationId)) {
				return app;
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return "JpaVirtualMachine [applications=" + applications + ", id=" + id + ", name=" + name + ", state=" + state
				+ ", os=" + os + ", hostname=" + hostname + ", sshPort=" + sshPort + ", infrastructureId="
				+ infrastructureId + ", userName=" + userName + ", privateKey=[protected], privateKeyName="
				+ privateKeyName + ", ipAddress=" + ipAddress + "]";
	}
}
