package com.ncc.savior.virtueadmin.infrastructure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.ncc.savior.virtueadmin.model.persistance.JpaVirtualMachine;
import com.ncc.savior.virtueadmin.model.persistance.JpaVirtualMachineTemplate;
import com.ncc.savior.virtueadmin.model.persistance.JpaVirtueInstance;
import com.ncc.savior.virtueadmin.model.persistance.JpaVirtueTemplate;
import com.ncc.savior.virtueadmin.model.persistance.JpaVirtueUser;

import net.bytebuddy.agent.VirtualMachine;

/**
 * Base class for cloud managers where actions on VM's in a virtue are split
 * between multiple {@link IVmManager}s based on some aspect of the
 * {@link VirtualMachine} or {@link VirtualMachineTemplate}.
 */
public abstract class BaseDelegatedCloudManager implements ICloudManager {

	@Override
	public void deleteVirtue(JpaVirtueInstance virtueInstance) {
		Collection<JpaVirtualMachine> vms = virtueInstance.getVms();
		Map<IVmManager, Collection<JpaVirtualMachine>> mapping = createVmManagerMappingFromVms(vms);
		for (Entry<IVmManager, Collection<JpaVirtualMachine>> entry : mapping.entrySet()) {
			IVmManager manager = entry.getKey();
			manager.deleteVirtualMachines(entry.getValue());
		}
	}

	@Override
	public JpaVirtueInstance createVirtue(JpaVirtueUser user, JpaVirtueTemplate template) throws Exception {
		Collection<JpaVirtualMachineTemplate> vmts = template.getVmTemplates();
		Collection<JpaVirtualMachine> vms = new ArrayList<JpaVirtualMachine>(vmts.size());
		Map<IVmManager, Collection<JpaVirtualMachineTemplate>> mapping = createVmManagerMappingFromVmTemplates(vmts);
		for (Entry<IVmManager, Collection<JpaVirtualMachineTemplate>> entry : mapping.entrySet()) {
			IVmManager manager = entry.getKey();
			Collection<JpaVirtualMachine> myVms = manager.provisionVirtualMachineTemplates(user, vmts);
			vms.addAll(myVms);
		}
		JpaVirtueInstance vi = new JpaVirtueInstance(template, user.getUsername(), vms);
		return vi;
	}

	@Override
	public JpaVirtueInstance startVirtue(JpaVirtueInstance virtueInstance) {
		Collection<JpaVirtualMachine> vms = virtueInstance.getVms();
		Map<IVmManager, Collection<JpaVirtualMachine>> mapping = createVmManagerMappingFromVms(vms);
		for (Entry<IVmManager, Collection<JpaVirtualMachine>> entry : mapping.entrySet()) {
			IVmManager manager = entry.getKey();
			manager.startVirtualMachines(entry.getValue());
		}
		return virtueInstance;
	}

	@Override
	public JpaVirtueInstance stopVirtue(JpaVirtueInstance virtueInstance) {
		Collection<JpaVirtualMachine> vms = virtueInstance.getVms();
		Map<IVmManager, Collection<JpaVirtualMachine>> mapping = createVmManagerMappingFromVms(vms);
		for (Entry<IVmManager, Collection<JpaVirtualMachine>> entry : mapping.entrySet()) {
			IVmManager manager = entry.getKey();
			manager.stopVirtualMachines(entry.getValue());
		}
		return virtueInstance;
	}

	protected Map<IVmManager, Collection<JpaVirtualMachine>> createVmManagerMappingFromVms(
			Collection<JpaVirtualMachine> vms) {
		HashMap<IVmManager, Collection<JpaVirtualMachine>> map = new HashMap<IVmManager, Collection<JpaVirtualMachine>>();
		for (JpaVirtualMachine vm : vms) {
			IVmManager manager = getVmManagerForVm(vm);
			Collection<JpaVirtualMachine> col = map.get(manager);
			if (col == null) {
				col = new ArrayList<JpaVirtualMachine>();
				map.put(manager, col);
			}
			col.add(vm);
		}
		return map;
	}

	protected Map<IVmManager, Collection<JpaVirtualMachineTemplate>> createVmManagerMappingFromVmTemplates(
			Collection<JpaVirtualMachineTemplate> vmts) {
		HashMap<IVmManager, Collection<JpaVirtualMachineTemplate>> map = new HashMap<IVmManager, Collection<JpaVirtualMachineTemplate>>();
		for (JpaVirtualMachineTemplate vmt : vmts) {
			IVmManager manager = getVmManagerForVmTemplate(vmt);
			Collection<JpaVirtualMachineTemplate> col = map.get(manager);
			if (col == null) {
				col = new ArrayList<JpaVirtualMachineTemplate>();
				map.put(manager, col);
			}
			col.add(vmt);
		}
		return map;
	}

	protected abstract IVmManager getVmManagerForVm(JpaVirtualMachine vm);

	protected abstract IVmManager getVmManagerForVmTemplate(JpaVirtualMachineTemplate vm);
}
