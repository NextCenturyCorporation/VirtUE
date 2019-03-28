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
package com.ncc.savior.virtueadmin.virtue;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VirtueInstance;
import com.ncc.savior.virtueadmin.model.VirtueTemplate;
import com.ncc.savior.virtueadmin.model.VirtueUser;
import com.ncc.savior.virtueadmin.virtue.ActiveVirtueManager.VirtueCreationDeletionListener;

/**
 * Manages and controls active {@link VirtueInstance}s for the system. This
 * class is what converts {@link VirtueTemplate}s into {@link VirtueInstance}.
 * 
 *
 */
public interface IActiveVirtueManager {

	/**
	 * Returns all virtues that are assigned to the given user and were created from
	 * one of the given {@link VirtueTemplate} ids.
	 * 
	 * @param user
	 * @param keySet
	 * @return
	 */
	Map<String, Set<VirtueInstance>> getVirtuesFromTemplateIds(VirtueUser user, Set<String> keySet);

	/**
	 * Returns {@link VirtualMachine} that contains the given application in the
	 * given virtue.
	 * 
	 * @param virtueId
	 * @param applicationId
	 * @return
	 */
	VirtualMachine getVmWithApplication(String virtueId, String applicationId);

	/**
	 * Initiates starting a virtual machine.
	 * 
	 * @param vm
	 * @return
	 */
	VirtualMachine startVirtualMachine(VirtualMachine vm);

	/**
	 * Handles the provisioning of all resources needed for a {@link VirtueTemplate}
	 * to create a usable {@link VirtueInstance}.
	 * 
	 * @param user
	 * @param template
	 * @return
	 */
	VirtueInstance provisionTemplate(VirtueUser user, VirtueTemplate template);

	VirtueInstance deleteVirtue(VirtueUser user, String instanceId);

	Iterable<VirtueInstance> getAllActiveVirtues();

	VirtueInstance getActiveVirtue(String virtueId);

	Collection<VirtueInstance> getVirtuesForUser(VirtueUser user);

	VirtueInstance getVirtueForUserFromTemplateId(VirtueUser user, String instanceId);

	void adminDeleteVirtue(String instanceId);

	VirtueInstance startVirtue(VirtueUser user, String virtueId);

	VirtueInstance stopVirtue(VirtueUser user, String virtueId);

	VirtualMachine getVm(String id);

	Iterable<VirtualMachine> getAllVirtualMachines();

	void rebootVm(String vmId);

	void sync();

	void addVirtueCreationDeletionListener(VirtueCreationDeletionListener vcdl);

}
