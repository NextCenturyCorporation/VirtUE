package com.ncc.savior.virtueadmin.infrastructure.staticvm;

import java.util.Collection;
import java.util.UUID;

import com.ncc.savior.virtueadmin.infrastructure.ICloudManager;
import com.ncc.savior.virtueadmin.model.VmState;

import persistance.JpaVirtualMachine;
import persistance.JpaVirtualMachineTemplate;
import persistance.JpaVirtueInstance;
import persistance.JpaVirtueTemplate;
import persistance.JpaVirtueUser;

/**
 * Cloud manager that is backed by a single machine from
 * {@link StaticMachineVmManager}.
 */
public class StaticMachineCloudManager implements ICloudManager {

	private StaticMachineVmManager vmManager;

	public StaticMachineCloudManager(StaticMachineVmManager vmManager) {
		this.vmManager = vmManager;
	}

	@Override
	public void deleteVirtue(JpaVirtueInstance virtueInstance) {
		// do nothing
	}

	@Override
	public JpaVirtueInstance createVirtue(JpaVirtueUser user, JpaVirtueTemplate template) throws Exception {
		Collection<JpaVirtualMachineTemplate> templates = template.getVmTemplates();
		Collection<JpaVirtualMachine> vms = vmManager.provisionVirtualMachineTemplates(user, templates);
		JpaVirtueInstance virtue = new JpaVirtueInstance(UUID.randomUUID().toString(), template.getName(),
				user.getUsername(),
				template.getId(), template.getApplications(), vms);
		return virtue;
	}

	@Override
	public JpaVirtueInstance startVirtue(JpaVirtueInstance virtueInstance) {
		// single machine should always be started with this implementation.
		for (JpaVirtualMachine vm : virtueInstance.getVms()) {
			vm.setState(VmState.RUNNING);
		}
		return virtueInstance;
	}

	@Override
	public JpaVirtueInstance stopVirtue(JpaVirtueInstance virtueInstance) {
		// single machine should always be started with this implementation.
		for (JpaVirtualMachine vm : virtueInstance.getVms()) {
			vm.setState(VmState.STOPPED);
		}
		return virtueInstance;
	}

}
