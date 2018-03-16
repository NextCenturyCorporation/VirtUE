package com.ncc.savior.virtueadmin.model.dto;

import java.util.Collection;

import com.ncc.savior.virtueadmin.model.BaseVirtualMachine;
import com.ncc.savior.virtueadmin.model.OS;
import com.ncc.savior.virtueadmin.model.VmState;

public class VirtualMachineDto extends BaseVirtualMachine {
	private Collection<String> applicationIds;

	protected VirtualMachineDto(String id, String name, Collection<String> applicationIds, VmState state, OS os,
			String infrastructureId, String hostname, int sshPort, String userName, String privateKey,
			String privateKeyName, String ipAddress) {
		super(id, name, state, os, infrastructureId, hostname, sshPort, userName, privateKey, privateKeyName,
				ipAddress);
		this.applicationIds = applicationIds;
	}

	/**
	 * Used for Jackson deserialization.
	 */
	protected VirtualMachineDto() {

	}

	public Collection<String> getApplicationIds() {
		return applicationIds;
	}

	public void setApplicationIds(Collection<String> applicationIds) {
		this.applicationIds = applicationIds;
	}

	@Override
	public String toString() {
		return "RestVirtualMachine [applicationIds=" + applicationIds + ", id=" + id + ", name=" + name + ", state="
				+ state + ", os=" + os + ", hostname=" + hostname + ", sshPort=" + sshPort + ", infrastructureId="
				+ infrastructureId + ", userName=" + userName + ", privateKey=[protected], privateKeyName="
				+ privateKeyName + ", ipAddress=" + ipAddress + "]";
	}
}
