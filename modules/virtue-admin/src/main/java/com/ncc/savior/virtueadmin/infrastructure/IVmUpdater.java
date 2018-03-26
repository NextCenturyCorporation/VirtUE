package com.ncc.savior.virtueadmin.infrastructure;

import java.util.Collection;

import com.ncc.savior.virtueadmin.model.VirtualMachine;

public interface IVmUpdater {

	/**
	 * Adds VMs to the provisioning pipeline. The provisioning pipeline leads the
	 * VMs through a series of tasks each at individual rates. The tasks are:
	 * <ol>
	 * <li>Rename AWS VM
	 * <li>Get networking information from AWS
	 * <li>Test reachability of VM and then add unique RSA key
	 * <li>Start Xpra server
	 * 
	 * @param vms
	 */
	void addVmToProvisionPipeline(Collection<VirtualMachine> vms);

	void addVmsToStartingPipeline(Collection<VirtualMachine> vms);

	void addVmsToStoppingPipeline(Collection<VirtualMachine> vms);

	void addVmsToDeletingPipeline(Collection<VirtualMachine> vms);


}