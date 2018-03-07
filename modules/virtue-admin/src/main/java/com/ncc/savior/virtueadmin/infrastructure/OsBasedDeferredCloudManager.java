package com.ncc.savior.virtueadmin.infrastructure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import com.ncc.savior.virtueadmin.model.OS;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VirtualMachineTemplate;
import com.ncc.savior.virtueadmin.model.VirtueInstance;
import com.ncc.savior.virtueadmin.model.VirtueTemplate;
import com.ncc.savior.virtueadmin.model.VirtueUser;
import com.ncc.savior.virtueadmin.util.SaviorException;

/**
 * {@link ICloudManager} implementation which defers individual VM actions to
 * multiple {@link IVmManager}s based on the OS of the VM.
 *
 */
public class OsBasedDeferredCloudManager extends BaseDeferredCloudManager {

	private Map<OS, IVmManager> managerMap;

	public OsBasedDeferredCloudManager(Map<OS, IVmManager> managerMap) {
		this.managerMap = managerMap;
	}

	@Override
	public void deleteVirtue(VirtueInstance virtueInstance) {
		Collection<VirtualMachine> vms = virtueInstance.getVms();
		Map<IVmManager, Collection<VirtualMachine>> mapping = createVmManagerMappingFromVms(vms);
		for (Entry<IVmManager, Collection<VirtualMachine>> entry : mapping.entrySet()) {
			IVmManager manager = entry.getKey();
			manager.deleteVirtualMachines(entry.getValue());
		}
	}

	@Override
	public VirtueInstance createVirtue(VirtueUser user, VirtueTemplate template) throws Exception {
		Collection<VirtualMachineTemplate> vmts = template.getVmTemplates();
		Collection<VirtualMachine> vms = new ArrayList<VirtualMachine>(vmts.size());
		Map<IVmManager, Collection<VirtualMachineTemplate>> mapping = createVmManagerMappingFromVmTemplates(vmts);
		for (Entry<IVmManager, Collection<VirtualMachineTemplate>> entry : mapping.entrySet()) {
			IVmManager manager = entry.getKey();
			Collection<VirtualMachine> myVms = manager.provisionVirtualMachineTemplates(user, vmts);
			vms.addAll(myVms);
		}
		VirtueInstance vi = new VirtueInstance(template, user.getUsername(), vms);
		return vi;
	}

	@Override
	protected IVmManager getVmManagerForVm(VirtualMachine vm) {
		IVmManager manager = managerMap.get(vm.getOs());
		if (manager == null) {
			throw new SaviorException(SaviorException.CONFIGURATION_ERROR,
					this.getClass().getCanonicalName() + " not configured with IVmManager for OS=" + vm.getOs());
		}
		return manager;
	}

	@Override
	protected IVmManager getVmManagerForVmTemplate(VirtualMachineTemplate vmt) {
		IVmManager manager = managerMap.get(vmt.getOs());
		if (manager == null) {
			throw new SaviorException(SaviorException.CONFIGURATION_ERROR,
					this.getClass().getCanonicalName() + " not configured with IVmManager for OS=" + vmt.getOs());
		}
		return manager;
	}
}
