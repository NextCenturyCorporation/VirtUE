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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VirtueInstance;
import com.ncc.savior.virtueadmin.model.VirtueUser;
import com.ncc.savior.virtueadmin.model.VmState;

/**
 * Data access object for Active Virtues. This class manages the storage and
 * retrieval of Virtue information from a data storage system.
 * 
 *
 */
public interface IActiveVirtueDao {

	/**
	 * Returns all virtues that are assigned to the given user and are build from
	 * one of the given templateIds.
	 * 
	 * @param user
	 * 
	 * @param templateIds
	 * @return
	 */
	Map<String, Set<VirtueInstance>> getVirtuesFromTemplateIds(VirtueUser user, Set<String> templateIds);

	/**
	 * Updates the state of a given VM.
	 * 
	 * @param vmId
	 * @param state
	 */
	void updateVmState(String vmId, VmState state);

	/**
	 * Return the given Virtual Machine in a virtue that has the given application.
	 * 
	 * @param virtueId
	 * @param applicationId
	 * @return
	 */
	VirtualMachine getVmWithApplication(String virtueId, String applicationId);

	/**
	 * Adds a new virtue to the data store.
	 * 
	 * @param vi
	 */
	void addVirtue(VirtueInstance vi);

	Optional<VirtueInstance> getVirtueInstance(String virtueId);

	Iterable<VirtueInstance> getAllActiveVirtues();

	void clear();

	Collection<VirtueInstance> getVirtuesForUser(VirtueUser user);

	VirtueInstance getVirtueInstance(VirtueUser user, String instanceId);

	void updateVms(Collection<VirtualMachine> vms);

	void deleteVirtue(VirtueInstance vi);

	void updateVirtue(VirtueInstance virtue);

	Optional<VirtualMachine> getXenVm(String id);
	
	List<VirtualMachine> getVmWithNameStartsWith(String startsWith);

	Iterable<VirtueInstance> getVirtueInstances(Collection<String> virtueList);

	Iterable<VirtualMachine> getAllVirtualMachines();

	void deleteVm(VirtualMachine vm);
	
	VirtueInstance getVirtue(VirtualMachine vm);

	VirtueInstance getVirtueByVmId(String id);

}
