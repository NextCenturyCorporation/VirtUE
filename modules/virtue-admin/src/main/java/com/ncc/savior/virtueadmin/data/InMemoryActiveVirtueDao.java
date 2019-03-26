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
package com.ncc.savior.virtueadmin.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.ncc.savior.util.SaviorErrorCode;
import com.ncc.savior.util.SaviorException;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VirtueInstance;
import com.ncc.savior.virtueadmin.model.VirtueUser;
import com.ncc.savior.virtueadmin.model.VmState;

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
	public Map<String, Set<VirtueInstance>> getVirtuesFromTemplateIds(VirtueUser user, Set<String> templateIds) {
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
	public Collection<VirtueInstance> getVirtuesForUser(VirtueUser user) {
		List<VirtueInstance> result = new ArrayList<VirtueInstance>();
		Collection<VirtueInstance> vs = virtues.values();
		for (VirtueInstance v : vs) {
			if (v.getUsername().equals(user.getUsername())) {
				result.add(v);
			}
		}
		return result;
	}

	@Override
	public VirtueInstance getVirtueInstance(VirtueUser user, String instanceId) {
		VirtueInstance vi = virtues.get(instanceId);
		if (vi != null && vi.getUsername().equals(user.getUsername())) {
			return vi;
		}
		return null;
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
			throw new SaviorException(SaviorErrorCode.APPLICATION_NOT_FOUND,
					"Cannot find application with ID=" + applicationId + " in virtue id=" + virtueId);
		} else {
			throw new SaviorException(SaviorErrorCode.VIRTUE_NOT_FOUND, "Cannot find virtue with ID=" + virtueId);
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

	@Override
	public void updateVms(Collection<VirtualMachine> vms) {
		throw new SaviorException(SaviorErrorCode.NOT_IMPLEMENTED,
				"update vm not implemented for " + this.getClass().getSimpleName());
	}

	@Override
	public void deleteVirtue(VirtueInstance vi) {
		throw new SaviorException(SaviorErrorCode.NOT_IMPLEMENTED,
				"delete virtue not implemented for " + this.getClass().getSimpleName());
	}

	@Override
	public void updateVirtue(VirtueInstance virtue) {
		throw new SaviorException(SaviorErrorCode.NOT_IMPLEMENTED,
				"update virtue not implemented for " + this.getClass().getSimpleName());
	}

	@Override
	public Optional<VirtualMachine> getXenVm(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterable<VirtueInstance> getVirtueInstances(Collection<String> virtueList) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterable<VirtualMachine> getAllVirtualMachines() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteVm(VirtualMachine vm) {
		// TODO Auto-generated method stub

	}

	@Override
	public VirtueInstance getVirtue(VirtualMachine vm) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public VirtueInstance getVirtueByVmId(String id) {
		// TODO Auto-generated method stub
		return null;
	}
}
