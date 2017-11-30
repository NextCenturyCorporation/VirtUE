package com.ncc.savior.virtueadmin.data;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.User;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VirtualMachineTemplate;
import com.ncc.savior.virtueadmin.model.VirtueInstance;
import com.ncc.savior.virtueadmin.model.VirtueTemplate;
import com.ncc.savior.virtueadmin.model.VmState;
import com.ncc.savior.virtueadmin.util.SaviorException;

public class InMemoryActiveVirtueDao implements IActiveVirtueDao, ITemplateManager {

	private Map<String, VirtueInstance> virtues;

	public InMemoryActiveVirtueDao() {
		virtues = new LinkedHashMap<String, VirtueInstance>();
	}

	@Override
	public Map<String, Set<VirtueInstance>> getVirtuesFromTemplateIds(Set<String> templateIds) {
		Map<String, Set<VirtueInstance>> map = new LinkedHashMap<String, Set<VirtueInstance>>();
		if (templateIds == null || templateIds.isEmpty()) {
			return map;
		}
		for (VirtueInstance instance : virtues.values()) {
			if (templateIds.contains(instance.getTemplateId())) {
				Set<VirtueInstance> virtuesFromTemplateId = map.get(instance.getTemplateId());
				if (virtuesFromTemplateId == null) {
					virtuesFromTemplateId = new LinkedHashSet<VirtueInstance>();
					map.put(instance.getTemplateId(), virtuesFromTemplateId);
				}
				virtuesFromTemplateId.add(instance);
			}
		}
		return map;
	}

	@Override
	public void updateVmState(String vmId, VmState state) {
		for (VirtueInstance instance : virtues.values()) {
			Map<String, VirtualMachine> vms = instance.getVms();
			if (vms.containsKey(vmId)) {
				vms.get(vmId).setState(state);
				break;
			}
		}
	}

	@Override
	public VirtualMachine getVmWithApplication(String virtueId, String applicationId) {
		VirtueInstance virtue = virtues.get(virtueId);
		if (virtue != null) {
			for (VirtualMachine vm : virtue.getVms().values()) {
				ApplicationDefinition app = vm.getApplications().get(applicationId);
				if (app != null) {
					return vm;
				}
			}
			// if we drop out of the list of vms, we couldn't find the vm with that
			// application id.
			throw new SaviorException(SaviorException.APPLICATION_ID_NOT_FOUND,
					"Cannot find application with ID=" + applicationId + " in virtue id=" + virtueId);
		} else {
			throw new SaviorException(SaviorException.VIRTUE_ID_NOT_FOUND, "Cannot find virtue with ID=" + virtueId);
		}
	}

	@Override
	public void addVirtue(VirtueInstance vi) {
		virtues.put(vi.getId(), vi);
	}

	@Override
	public Set<VirtueTemplate> getAllVirtueTemplates() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<VirtualMachineTemplate> getAllVirtualMachineTemplates() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, VirtueTemplate> getVirtueTemplatesForUser(User user) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<String> getVirtueTemplateIdsForUser(User user) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ApplicationDefinition getApplicationDefinition(String applicationId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public VirtueTemplate getTemplate(User user, String templateId) {
		// TODO Auto-generated method stub
		return null;
	}

}
