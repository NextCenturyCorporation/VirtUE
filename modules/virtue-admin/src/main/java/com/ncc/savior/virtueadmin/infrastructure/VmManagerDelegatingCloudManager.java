package com.ncc.savior.virtueadmin.infrastructure;

import java.util.Collection;

import persistance.JpaVirtualMachine;
import persistance.JpaVirtueInstance;
import persistance.JpaVirtueTemplate;
import persistance.JpaVirtueUser;


/**
 * Cloud manager that simply delegates its actions to a given
 * {@link IVmManager}. All actions are performed in the {@link IVmManager} given
 * in the constructor.
 *
 */

public class VmManagerDelegatingCloudManager implements ICloudManager {

	private IVmManager vmManager;

	public VmManagerDelegatingCloudManager(IVmManager vmManager) {
		this.vmManager = vmManager;
	}

	@Override
	public void deleteVirtue(JpaVirtueInstance virtueInstance) {
		vmManager.deleteVirtualMachines(virtueInstance.getVms());
	}

	@Override
	public JpaVirtueInstance createVirtue(JpaVirtueUser user, JpaVirtueTemplate template) throws Exception {
		Collection<JpaVirtualMachine> vms = vmManager.provisionVirtualMachineTemplates(user, template.getVmTemplates());
		JpaVirtueInstance vi = new JpaVirtueInstance(template, user.getUsername(), vms);
		return vi;
	}

	@Override
	public JpaVirtueInstance startVirtue(JpaVirtueInstance virtueInstance) {
		vmManager.startVirtualMachines(virtueInstance.getVms());
		return virtueInstance;
	}

	@Override
	public JpaVirtueInstance stopVirtue(JpaVirtueInstance virtueInstance) {
		vmManager.stopVirtualMachines(virtueInstance.getVms());
		return virtueInstance;
	}
}
