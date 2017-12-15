package com.ncc.savior.virtueadmin.data.jpa;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.ncc.savior.virtueadmin.data.IActiveVirtueDao;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.User;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VirtueInstance;
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
	public Map<String, Set<VirtueInstance>> getVirtuesFromTemplateIds(User user, Set<String> templateIds) {
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
	public void updateVmState(String vmId, VmState state) {
		VirtualMachine vm = vmRepository.findOne(vmId);
		if (vm != null) {
			vm.setState(state);
		} else {
			throw new SaviorException(SaviorException.VM_NOT_FOUND, "Unable to find virtual machine with id=" + vmId);
		}
		vmRepository.save(vm);
	}

	@Override
	public VirtualMachine getVmWithApplication(String virtueId, String applicationId) {
		// TODO could be more efficient
		VirtueInstance virtue = virtueRepository.findOne(virtueId);
		if (virtue == null) {
			throw new SaviorException(SaviorException.VIRTUE_ID_NOT_FOUND, "Unable to find virtue with id=" + virtueId);
		}
		Collection<VirtualMachine> vms = virtue.getVms();
		for (VirtualMachine vm : vms) {
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
	public void addVirtue(VirtueInstance vi) {
		virtueRepository.save(vi);

	}

}
