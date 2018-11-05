package com.ncc.savior.virtueadmin.infrastructure;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.ncc.savior.util.SaviorErrorCode;
import com.ncc.savior.util.SaviorException;
import com.ncc.savior.virtueadmin.infrastructure.aws.VirtueCreationAdditionalParameters;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VirtueInstance;
import com.ncc.savior.virtueadmin.model.VirtueTemplate;
import com.ncc.savior.virtueadmin.model.VirtueUser;

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
	public void deleteVirtue(VirtueInstance virtueInstance, CompletableFuture<VirtueInstance> future) {
		vmManager.deleteVirtualMachines(virtueInstance.getVms(), null);
	}

	@Override
	public VirtueInstance createVirtue(VirtueUser user, VirtueTemplate template) throws Exception {
		Collection<VirtualMachine> vms = vmManager.provisionVirtualMachineTemplates(user, template.getVmTemplates(),
				null, new VirtueCreationAdditionalParameters(template.getName()));
		VirtueInstance vi = new VirtueInstance(template, user.getUsername(), vms);
		return vi;
	}

	@Override
	public VirtueInstance startVirtue(VirtueInstance virtueInstance) {
		vmManager.startVirtualMachines(virtueInstance.getVms(), null);
		return virtueInstance;
	}

	@Override
	public VirtueInstance stopVirtue(VirtueInstance virtueInstance) {
		vmManager.stopVirtualMachines(virtueInstance.getVms(), null);
		return virtueInstance;
	}

	@Override
	public void rebootVm(VirtualMachine vm, String virtue) {
		// TODO Auto-generated method stub
	}

	@Override
	public void sync(List<String> ids) {
		throw new SaviorException(SaviorErrorCode.NOT_IMPLEMENTED, "Sync not implemented in this implementation");
	}
}
