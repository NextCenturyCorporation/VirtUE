package com.ncc.savior.virtueadmin.infrastructure;

import java.util.Collection;

import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VirtueInstance;
import com.ncc.savior.virtueadmin.model.VirtueTemplate;
import com.ncc.savior.virtueadmin.model.VirtueUser;

public class VmManagerDeferringCloudManager implements ICloudManager {

	private IVmManager vmManager;

	public VmManagerDeferringCloudManager(IVmManager vmManager) {
		// TODO make multiple vm managers a valid option with methods to give a set of
		// the VMs to each.
		this.vmManager = vmManager;
	}

	@Override
	public void deleteVirtue(VirtueInstance virtueInstance) {
		// TODO Auto-generated method stub

	}

	@Override
	public VirtueInstance createVirtue(VirtueUser user, VirtueTemplate template) throws Exception {
		Collection<VirtualMachine> vms = vmManager.provisionVirtualMachineTemplates(user, template.getVmTemplates());
		VirtueInstance vi = new VirtueInstance(template, user.getUsername(), vms);
		return vi;
	}

}
