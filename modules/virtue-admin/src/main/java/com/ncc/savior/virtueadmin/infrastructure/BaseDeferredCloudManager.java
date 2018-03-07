package com.ncc.savior.virtueadmin.infrastructure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VirtualMachineTemplate;

/**
 * Base class for cloud managers where VM's in a virtue are split between
 * multiple {@link IVmManager}s based on some aspect of the
 * {@link VirtualMachine} or {@link VirtualMachineTemplate}.
 */
public abstract class BaseDeferredCloudManager implements ICloudManager {
	protected Map<IVmManager, Collection<VirtualMachine>> createVmManagerMappingFromVms(
			Collection<VirtualMachine> vms) {
		HashMap<IVmManager, Collection<VirtualMachine>> map = new HashMap<IVmManager, Collection<VirtualMachine>>();
		for (VirtualMachine vm : vms) {
			IVmManager manager = getVmManagerForVm(vm);
			Collection<VirtualMachine> col = map.get(manager);
			if (col == null) {
				col = new ArrayList<VirtualMachine>();
				map.put(manager, col);
			}
			col.add(vm);
		}
		return map;
	}

	protected Map<IVmManager, Collection<VirtualMachineTemplate>> createVmManagerMappingFromVmTemplates(
			Collection<VirtualMachineTemplate> vmts) {
		HashMap<IVmManager, Collection<VirtualMachineTemplate>> map = new HashMap<IVmManager, Collection<VirtualMachineTemplate>>();
		for (VirtualMachineTemplate vmt : vmts) {
			IVmManager manager = getVmManagerForVmTemplate(vmt);
			Collection<VirtualMachineTemplate> col = map.get(manager);
			if (col == null) {
				col = new ArrayList<VirtualMachineTemplate>();
				map.put(manager, col);
			}
			col.add(vmt);
		}
		return map;
	}

	protected abstract IVmManager getVmManagerForVm(VirtualMachine vm);

	protected abstract IVmManager getVmManagerForVmTemplate(VirtualMachineTemplate vm);
}
