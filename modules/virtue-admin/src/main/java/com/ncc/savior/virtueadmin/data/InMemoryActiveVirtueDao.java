package com.ncc.savior.virtueadmin.data;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.User;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VirtueInstance;
import com.ncc.savior.virtueadmin.model.VmState;
import com.ncc.savior.virtueadmin.util.SaviorException;

/**
 * Implementation of {@link IActiveVirtueDao} that stores all the Active Virtue
 * data in memory. This implementation is for testing and demo purpose only and
 * should not be used in production.
 * 
 * See interface for function comments.
 *
 */
public class InMemoryActiveVirtueDao implements IActiveVirtueDao {

	private Map<String, VirtueInstance> virtues;

	public InMemoryActiveVirtueDao() {
		virtues = new LinkedHashMap<String, VirtueInstance>();
	}

	@Override
	public Map<String, Set<VirtueInstance>> getVirtuesFromTemplateIds(User user, Set<String> templateIds) {
		Map<String, Set<VirtueInstance>> map = new LinkedHashMap<String, Set<VirtueInstance>>();
		if (templateIds == null || templateIds.isEmpty()) {
			return map;
		}
		for (VirtueInstance instance : virtues.values()) {
			if (templateIds.contains(instance.getTemplateId())) {
				if (instance.getUsername() != null && instance.getUsername().equals(user.getUsername())) {
					Set<VirtueInstance> virtuesFromTemplateId = map.get(instance.getTemplateId());
					if (virtuesFromTemplateId == null) {
						virtuesFromTemplateId = new LinkedHashSet<VirtueInstance>();
						map.put(instance.getTemplateId(), virtuesFromTemplateId);
					}
					virtuesFromTemplateId.add(instance);
				}
			}
		}
		return map;
	}

	@Override
	public void updateVmState(String vmId, VmState state) {
		for (VirtueInstance instance : virtues.values()) {
			Collection<VirtualMachine> vms = instance.getVms();
			for (VirtualMachine vm : vms) {
				if (vm.getId().equals(vmId)) {
					vm.setState(state);
				break;
			}
			}
		}
	}

	@Override
	public VirtualMachine getVmWithApplication(String virtueId, String applicationId) {
		VirtueInstance virtue = virtues.get(virtueId);
		if (virtue != null) {
			for (VirtualMachine vm : virtue.getVms()) {
				ApplicationDefinition app = vm.findApplicationById(applicationId);
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
	public Optional<VirtueInstance> getVirtueInstance(String virtueId) {
		return Optional.of(virtues.get(virtueId));
	}

	@Override
	public Iterable<VirtueInstance> getAllActiveVirtues() {
		return virtues.values();
	}

	@Override
	public void clear() {
		virtues.clear();
	}
}
