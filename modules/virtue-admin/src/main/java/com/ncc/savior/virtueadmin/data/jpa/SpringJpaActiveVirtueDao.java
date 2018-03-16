package com.ncc.savior.virtueadmin.data.jpa;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.ncc.savior.virtueadmin.data.IActiveVirtueDao;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.VmState;
import com.ncc.savior.virtueadmin.model.persistance.JpaVirtualMachine;
import com.ncc.savior.virtueadmin.model.persistance.JpaVirtueInstance;
import com.ncc.savior.virtueadmin.model.persistance.JpaVirtueUser;
import com.ncc.savior.virtueadmin.util.SaviorException;

/**
 * Implementation of {@link IActiveVirtueDao} that uses Spring and JPA.
 */
@Repository
public class SpringJpaActiveVirtueDao implements IActiveVirtueDao {
	@Autowired
	private VirtualMachineRepository vmRepository;
	// @Autowired
	// private ApplicationDefinitionRepository appRepository;
	// @Autowired
	// private VirtualMachineTemplateRepository vmtRepository;
	@Autowired
	private VirtueInstanceRepository virtueRepository;

	@Override
	public Map<String, Set<JpaVirtueInstance>> getVirtuesFromTemplateIds(JpaVirtueUser user, Set<String> templateIds) {
		List<JpaVirtueInstance> virtueInstances = virtueRepository.findByUsernameAndTemplateIdIn(user.getUsername(),
				templateIds);
		Map<String, Set<JpaVirtueInstance>> templateIdToVirtueInstances = new HashMap<String, Set<JpaVirtueInstance>>();
		for (JpaVirtueInstance vi : virtueInstances) {
			Set<JpaVirtueInstance> set = templateIdToVirtueInstances.get(vi.getTemplateId());
			if (set == null) {
				set = new HashSet<JpaVirtueInstance>();
				templateIdToVirtueInstances.put(vi.getTemplateId(), set);
			}
			set.add(vi);
		}
		return templateIdToVirtueInstances;
	}

	@Override
	public Collection<JpaVirtueInstance> getVirtuesForUser(JpaVirtueUser user) {
		Collection<JpaVirtueInstance> virtueInstances = virtueRepository.findByUsername(user.getUsername());
		return virtueInstances;
	}

	@Override
	public JpaVirtueInstance getVirtueInstance(JpaVirtueUser user, String instanceId) {
		String username = user.getUsername();
		JpaVirtueInstance vi = virtueRepository.findByUsernameAndId(username, instanceId);
		return vi;
	}

	@Override
	public void updateVmState(String vmId, VmState state) {
		Optional<JpaVirtualMachine> vm = vmRepository.findById(vmId);
		if (!vm.isPresent()) {
			throw new SaviorException(SaviorException.VM_NOT_FOUND, "Unable to find virtual machine with id=" + vmId);
		}
		vm.get().setState(state);
		vmRepository.save(vm.get());
	}

	@Override
	public JpaVirtualMachine getVmWithApplication(String virtueId, String applicationId) {
		// TODO could be more efficient
		Optional<JpaVirtueInstance> virtue = virtueRepository.findById(virtueId);
		if (!virtue.isPresent()) {
			throw new SaviorException(SaviorException.VIRTUE_ID_NOT_FOUND, "Unable to find virtue with id=" + virtueId);
		}
		Collection<JpaVirtualMachine> vms = virtue.get().getVms();
		for (JpaVirtualMachine vm : vms) {
			Collection<ApplicationDefinition> apps = vm.getApplications();
			for (ApplicationDefinition app : apps) {
				if (app.getId().equals(applicationId)) {
					return vm;
				}
			}
		}
		throw new SaviorException(SaviorException.APPLICATION_ID_NOT_FOUND,
				"Unable to find a VM with application with id=" + applicationId + " in virtue with id=" + virtueId);
	}

	@Override
	public void addVirtue(JpaVirtueInstance vi) {
		for (JpaVirtualMachine vm : vi.getVms()) {
			vmRepository.save(vm);
		}
		virtueRepository.save(vi);
	}

	@Override
	public Optional<JpaVirtueInstance> getVirtueInstance(String virtueId) {
		return virtueRepository.findById(virtueId);
	}

	@Override
	public Iterable<JpaVirtueInstance> getAllActiveVirtues() {
		return virtueRepository.findAll();
	}

	@Override
	public void clear() {
		virtueRepository.deleteAll();
	}

	@Override
	public void updateVms(Collection<JpaVirtualMachine> vms) {
		vmRepository.saveAll(vms);
	}

}
