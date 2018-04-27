package com.ncc.savior.virtueadmin.infrastructure;

import java.util.Collection;

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
	public void deleteVirtue(VirtueInstance virtueInstance) {
		vmManager.deleteVirtualMachines(virtueInstance.getVms());
	}

	@Override
	public VirtueInstance createVirtue(VirtueUser user, VirtueTemplate template) throws Exception {
		Collection<VirtualMachine> vms = vmManager.provisionVirtualMachineTemplates(user, template.getVmTemplates());
		VirtueInstance vi = new VirtueInstance(template, user.getUsername(), vms);
		return vi;
	}

	@Override
	public VirtueInstance startVirtue(VirtueInstance virtueInstance) {
		vmManager.startVirtualMachines(virtueInstance.getVms());
		return virtueInstance;
	}

	@Override
	public VirtueInstance stopVirtue(VirtueInstance virtueInstance) {
		vmManager.stopVirtualMachines(virtueInstance.getVms());
		return virtueInstance;
	}
}
