package com.ncc.savior.virtueadmin.infrastructure.staticvm;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.ncc.savior.virtueadmin.infrastructure.ICloudManager;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VirtualMachineTemplate;
import com.ncc.savior.virtueadmin.model.VirtueInstance;
import com.ncc.savior.virtueadmin.model.VirtueTemplate;
import com.ncc.savior.virtueadmin.model.VirtueUser;
import com.ncc.savior.virtueadmin.model.VmState;

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
	public void deleteVirtue(VirtueInstance virtueInstance, CompletableFuture<VirtueInstance> future) {
		// do nothing
	}

	@Override
	public VirtueInstance createVirtue(VirtueUser user, VirtueTemplate template) throws Exception {
		Collection<VirtualMachineTemplate> templates = template.getVmTemplates();
		Collection<VirtualMachine> vms = vmManager.provisionVirtualMachineTemplates(user, templates, null, template.getName());
		VirtueInstance virtue = new VirtueInstance(UUID.randomUUID().toString(), template.getName(), user.getUsername(),
				template.getId(), template.getApplications(), vms);
		return virtue;
	}

	@Override
	public VirtueInstance startVirtue(VirtueInstance virtueInstance) {
		// single machine should always be started with this implementation.
		for(VirtualMachine vm:virtueInstance.getVms()) {
			vm.setState(VmState.RUNNING);
		}
		return virtueInstance;
	}

	@Override
	public VirtueInstance stopVirtue(VirtueInstance virtueInstance) {
		// single machine should always be started with this implementation.
		for (VirtualMachine vm : virtueInstance.getVms()) {
			vm.setState(VmState.STOPPED);
		}
		return virtueInstance;
	}

	@Override
	public void rebootVm(VirtualMachine vm, String virtue) {
		// TODO Auto-generated method stub
		
	}

}
