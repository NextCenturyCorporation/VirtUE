/*
 * Copyright (C) 2019 Next Century Corporation
 * 
 * This file may be redistributed and/or modified under either the GPL
 * 2.0 or 3-Clause BSD license. In addition, the U.S. Government is
 * granted government purpose rights. For details, see the COPYRIGHT.TXT
 * file at the root of this project.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 * 
 * SPDX-License-Identifier: (GPL-2.0-only OR BSD-3-Clause)
 */
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

import com.ncc.savior.util.SaviorErrorCode;
import com.ncc.savior.util.SaviorException;
import com.ncc.savior.virtueadmin.data.IActiveVirtueDao;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VirtueInstance;
import com.ncc.savior.virtueadmin.model.VirtueUser;
import com.ncc.savior.virtueadmin.model.VmState;

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
		Optional<VirtualMachine> vm = vmRepository.findById(vmId);
		if (!vm.isPresent()) {
			throw new SaviorException(SaviorErrorCode.VM_NOT_FOUND, "Unable to find virtual machine with id=" + vmId);
		}
		vm.get().setState(state);
		vmRepository.save(vm.get());
	}

	@Override
	public VirtualMachine getVmWithApplication(String virtueId, String applicationId) {
		// TODO could be more efficient
		Optional<VirtueInstance> virtue = virtueRepository.findById(virtueId);
		if (!virtue.isPresent()) {
			throw new SaviorException(SaviorErrorCode.VIRTUE_NOT_FOUND, "Unable to find virtue with id=" + virtueId);
		}
		Collection<VirtualMachine> vms = virtue.get().getVms();
		for (VirtualMachine vm : vms) {
			Collection<ApplicationDefinition> apps = vm.getApplications();
			for (ApplicationDefinition app : apps) {
				if (app.getId().equals(applicationId)) {
					return vm;
				}
			}
		}
		throw new SaviorException(SaviorErrorCode.APPLICATION_NOT_FOUND,
				"Unable to find a VM with application with id=" + applicationId + " in virtue with id=" + virtueId);
	}

	@Override
	public void addVirtue(VirtueInstance vi) {
		for (VirtualMachine vm : vi.getVms()) {
			vmRepository.save(vm);
		}
		virtueRepository.save(vi);
	}

	@Override
	public Optional<VirtueInstance> getVirtueInstance(String virtueId) {
		return virtueRepository.findById(virtueId);
	}

	@Override
	public Iterable<VirtueInstance> getVirtueInstances(Collection<String> virtueList) {
		return virtueRepository.findAllById(virtueList);
	}

	@Override
	public Iterable<VirtueInstance> getAllActiveVirtues() {
		return virtueRepository.findAll();
	}

	@Override
	public void clear() {
		virtueRepository.deleteAll();
		vmRepository.deleteAll();
	}

	@Override
	public void updateVms(Collection<VirtualMachine> vms) {
		vmRepository.saveAll(vms);
	}

	@Override
	public void deleteVirtue(VirtueInstance vi) {
		virtueRepository.delete(vi);
	}

	@Override
	public void updateVirtue(VirtueInstance virtue) {
		virtueRepository.save(virtue);
	}

	@Override
	public Optional<VirtualMachine> getXenVm(String id) {
		return vmRepository.findById(id);
	}

	@Override
	public Iterable<VirtualMachine> getAllVirtualMachines() {
		return vmRepository.findAll();
	}

	@Override
	public void deleteVm(VirtualMachine vm) {
		vmRepository.deleteById(vm.getId());
	}

	@Override
	public VirtueInstance getVirtue(VirtualMachine vm) {
		VirtueInstance vi = virtueRepository.findByVms(vm);
		return vi;
	}

	@Override
	public VirtueInstance getVirtueByVmId(String id) {
		return virtueRepository.findByVms_Id(id);
	}
}
