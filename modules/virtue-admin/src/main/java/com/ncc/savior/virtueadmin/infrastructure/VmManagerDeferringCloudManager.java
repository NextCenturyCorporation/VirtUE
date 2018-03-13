package com.ncc.savior.virtueadmin.infrastructure;

import java.util.Collection;

import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VirtueInstance;
import com.ncc.savior.virtueadmin.model.VirtueTemplate;
import com.ncc.savior.virtueadmin.model.VirtueUser;

/**
 * Cloud manager that simply defers its actions to a give {@link IVmManager}.
 *
 */

public class VmManagerDeferringCloudManager implements ICloudManager {

	private IVmManager vmManager;

	public VmManagerDeferringCloudManager(IVmManager vmManager) {
		// TODO make multiple vm managers a valid option with methods to give a set of
		// the VMs to each.
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
