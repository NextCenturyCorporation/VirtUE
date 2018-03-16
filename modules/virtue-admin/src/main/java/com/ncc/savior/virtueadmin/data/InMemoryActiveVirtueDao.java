package com.ncc.savior.virtueadmin.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang.NotImplementedException;

import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.VmState;
import com.ncc.savior.virtueadmin.model.persistance.JpaVirtualMachine;
import com.ncc.savior.virtueadmin.model.persistance.JpaVirtueInstance;
import com.ncc.savior.virtueadmin.model.persistance.JpaVirtueUser;
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

	private Map<String, JpaVirtueInstance> virtues;

	public InMemoryActiveVirtueDao() {
		virtues = new LinkedHashMap<String, JpaVirtueInstance>();
	}

	@Override
	public Map<String, Set<JpaVirtueInstance>> getVirtuesFromTemplateIds(JpaVirtueUser user, Set<String> templateIds) {
		Map<String, Set<JpaVirtueInstance>> map = new LinkedHashMap<String, Set<JpaVirtueInstance>>();
		if (templateIds == null || templateIds.isEmpty()) {
			return map;
		}
		for (JpaVirtueInstance instance : virtues.values()) {
			if (templateIds.contains(instance.getTemplateId())) {
				if (instance.getUsername() != null && instance.getUsername().equals(user.getUsername())) {
					Set<JpaVirtueInstance> virtuesFromTemplateId = map.get(instance.getTemplateId());
					if (virtuesFromTemplateId == null) {
						virtuesFromTemplateId = new LinkedHashSet<JpaVirtueInstance>();
						map.put(instance.getTemplateId(), virtuesFromTemplateId);
					}
					virtuesFromTemplateId.add(instance);
				}
			}
		}
		return map;
	}

	@Override
	public Collection<JpaVirtueInstance> getVirtuesForUser(JpaVirtueUser user) {
		List<JpaVirtueInstance> result = new ArrayList<JpaVirtueInstance>();
		Collection<JpaVirtueInstance> vs = virtues.values();
		for (JpaVirtueInstance v : vs) {
			if (v.getUsername().equals(user.getUsername())) {
				result.add(v);
			}
		}
		return result;
	}

	@Override
	public JpaVirtueInstance getVirtueInstance(JpaVirtueUser user, String instanceId) {
		JpaVirtueInstance vi = virtues.get(instanceId);
		if (vi != null && vi.getUsername().equals(user.getUsername())) {
			return vi;
		}
		return null;
	}

	@Override
	public void updateVmState(String vmId, VmState state) {
		for (JpaVirtueInstance instance : virtues.values()) {
			Collection<JpaVirtualMachine> vms = instance.getVms();
			for (JpaVirtualMachine vm : vms) {
				if (vm.getId().equals(vmId)) {
					vm.setState(state);
				break;
			}
			}
		}
	}

	@Override
	public JpaVirtualMachine getVmWithApplication(String virtueId, String applicationId) {
		JpaVirtueInstance virtue = virtues.get(virtueId);
		if (virtue != null) {
			for (JpaVirtualMachine vm : virtue.getVms()) {
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
	public void addVirtue(JpaVirtueInstance vi) {
		virtues.put(vi.getId(), vi);
	}

	@Override
	public Optional<JpaVirtueInstance> getVirtueInstance(String virtueId) {
		return Optional.of(virtues.get(virtueId));
	}

	@Override
	public Iterable<JpaVirtueInstance> getAllActiveVirtues() {
		return virtues.values();
	}

	@Override
	public void clear() {
		virtues.clear();
	}

	@Override
	public void updateVms(Collection<JpaVirtualMachine> vms) {
		throw new NotImplementedException();
	}
}
