package com.ncc.savior.virtueadmin.infrastructure;

import java.util.Collection;
import java.util.UUID;

import com.ncc.savior.virtueadmin.model.VirtueUser;
import com.ncc.savior.virtueadmin.model.AbstractVirtualMachine;
import com.ncc.savior.virtueadmin.model.VirtualMachineTemplate;
import com.ncc.savior.virtueadmin.model.VirtueInstance;
import com.ncc.savior.virtueadmin.model.VirtueTemplate;

public class StaticMachineCloudManager implements ICloudManager {

	private StaticMachineVmManager vmManager;

	public StaticMachineCloudManager(StaticMachineVmManager vmManager) {
		this.vmManager = vmManager;
	}

	@Override
	public void deleteVirtue(VirtueInstance virtueInstance) {
		// do nothing
	}

	@Override
	public VirtueInstance createVirtue(VirtueUser user, VirtueTemplate template) throws Exception {
		Collection<VirtualMachineTemplate> templates = template.getVmTemplates();
		Collection<AbstractVirtualMachine> vms = vmManager.provisionVirtualMachineTemplates(templates);
		VirtueInstance virtue = new VirtueInstance(UUID.randomUUID().toString(), template.getName(), user.getUsername(),
				template.getId(), template.getApplications(), vms);
		return virtue;
	}

}
