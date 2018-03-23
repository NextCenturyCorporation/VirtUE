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
import com.ncc.savior.virtueadmin.model.AbstractVirtualMachine;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.VirtueInstance;
import com.ncc.savior.virtueadmin.model.VirtueUser;
import com.ncc.savior.virtueadmin.model.VmState;
import com.ncc.savior.virtueadmin.util.SaviorException;

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
	public Map<String, Set<VirtueInstance>> getVirtuesFromTemplateIds(VirtueUser user, Set<String> templateIds) {
		List<VirtueInstance> virtueInstances = virtueRepository.findByUsernameAndTemplateIdIn(user.getUsername(),
				templateIds);
		Map<String, Set<VirtueInstance>> templateIdToVirtueInstances = new HashMap<String, Set<VirtueInstance>>();
		for (VirtueInstance vi : virtueInstances) {
			Set<VirtueInstance> set = templateIdToVirtueInstances.get(vi.getTemplateId());
			if (set == null) {
				set = new HashSet<VirtueInstance>();
				templateIdToVirtueInstances.put(vi.getTemplateId(), set);
			}
			set.add(vi);
		}
		return templateIdToVirtueInstances;
	}

	@Override
	public Collection<VirtueInstance> getVirtuesForUser(VirtueUser user) {
		Collection<VirtueInstance> virtueInstances = virtueRepository.findByUsername(user.getUsername());
		return virtueInstances;
	}

	@Override
	public VirtueInstance getVirtueInstance(VirtueUser user, String instanceId) {
		String username = user.getUsername();
		VirtueInstance vi = virtueRepository.findByUsernameAndId(username, instanceId);
		return vi;
	}

	@Override
	public void updateVmState(String vmId, VmState state) {
		Optional<AbstractVirtualMachine> vm = vmRepository.findById(vmId);
		if (!vm.isPresent()) {
			throw new SaviorException(SaviorException.ErrorCode.VM_NOT_FOUND,
					"Unable to find virtual machine with id=" + vmId);
		}
		vm.get().setState(state);
		vmRepository.save(vm.get());
	}

	@Override
	public AbstractVirtualMachine getVmWithApplication(String virtueId, String applicationId) {
		// TODO could be more efficient
		Optional<VirtueInstance> virtue = virtueRepository.findById(virtueId);
		if (!virtue.isPresent()) {
			throw new SaviorException(SaviorException.ErrorCode.VIRTUE_ID_NOT_FOUND,
					"Unable to find virtue with id=" + virtueId);
		}
		Collection<AbstractVirtualMachine> vms = virtue.get().getVms();
		for (AbstractVirtualMachine vm : vms) {
			Collection<ApplicationDefinition> apps = vm.getApplications();
			for (ApplicationDefinition app : apps) {
				if (app.getId().equals(applicationId)) {
					return vm;
				}
			}
		}
		throw new SaviorException(SaviorException.ErrorCode.APPLICATION_ID_NOT_FOUND,
				"Unable to find a VM with application with id=" + applicationId + " in virtue with id=" + virtueId);
	}

	@Override
	public void addVirtue(VirtueInstance vi) {
		for (AbstractVirtualMachine vm : vi.getVms()) {
			vmRepository.save(vm);
		}
		virtueRepository.save(vi);
	}

	@Override
	public Optional<VirtueInstance> getVirtueInstance(String virtueId) {
		return virtueRepository.findById(virtueId);
	}

	@Override
	public Iterable<VirtueInstance> getAllActiveVirtues() {
		return virtueRepository.findAll();
	}

	@Override
	public void clear() {
		virtueRepository.deleteAll();
	}

}
