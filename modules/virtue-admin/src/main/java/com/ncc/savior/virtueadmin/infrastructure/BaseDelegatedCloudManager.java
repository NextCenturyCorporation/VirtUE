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
package com.ncc.savior.virtueadmin.infrastructure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;

import com.ncc.savior.virtueadmin.infrastructure.aws.VirtueCreationAdditionalParameters;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VirtualMachineTemplate;
import com.ncc.savior.virtueadmin.model.VirtueInstance;
import com.ncc.savior.virtueadmin.model.VirtueTemplate;
import com.ncc.savior.virtueadmin.model.VirtueUser;

/**
 * Base class for cloud managers where actions on VM's in a virtue are split
 * between multiple {@link IVmManager}s based on some aspect of the
 * {@link VirtualMachine} or {@link VirtualMachineTemplate}.
 */
public abstract class BaseDelegatedCloudManager implements ICloudManager {

	@Override
	public void deleteVirtue(VirtueInstance virtueInstance, CompletableFuture<VirtueInstance> future) {
		Collection<VirtualMachine> vms = virtueInstance.getVms();
		Map<IVmManager, Collection<VirtualMachine>> mapping = createVmManagerMappingFromVms(vms);
		for (Entry<IVmManager, Collection<VirtualMachine>> entry : mapping.entrySet()) {
			IVmManager manager = entry.getKey();
			manager.deleteVirtualMachines(entry.getValue(), null);
		}
	}

	@Override
	public VirtueInstance createVirtue(VirtueUser user, VirtueTemplate template) throws Exception {
		Collection<VirtualMachineTemplate> vmts = template.getVmTemplates();
		Collection<VirtualMachine> vms = new ArrayList<VirtualMachine>(vmts.size());
		Map<IVmManager, Collection<VirtualMachineTemplate>> mapping = createVmManagerMappingFromVmTemplates(vmts);
		for (Entry<IVmManager, Collection<VirtualMachineTemplate>> entry : mapping.entrySet()) {
			IVmManager manager = entry.getKey();
			VirtueCreationAdditionalParameters virtueMod = new VirtueCreationAdditionalParameters(template.getName());
			Collection<VirtualMachine> myVms = manager.provisionVirtualMachineTemplates(user, vmts, null, virtueMod);
			vms.addAll(myVms);
		}
		VirtueInstance vi = new VirtueInstance(template, user.getUsername(), vms);
		return vi;
	}

	@Override
	public VirtueInstance startVirtue(VirtueInstance virtueInstance) {
		Collection<VirtualMachine> vms = virtueInstance.getVms();
		Map<IVmManager, Collection<VirtualMachine>> mapping = createVmManagerMappingFromVms(vms);
		for (Entry<IVmManager, Collection<VirtualMachine>> entry : mapping.entrySet()) {
			IVmManager manager = entry.getKey();
			manager.startVirtualMachines(entry.getValue(), null);
		}
		return virtueInstance;
	}

	@Override
	public VirtueInstance stopVirtue(VirtueInstance virtueInstance) {
		Collection<VirtualMachine> vms = virtueInstance.getVms();
		Map<IVmManager, Collection<VirtualMachine>> mapping = createVmManagerMappingFromVms(vms);
		for (Entry<IVmManager, Collection<VirtualMachine>> entry : mapping.entrySet()) {
			IVmManager manager = entry.getKey();
			manager.stopVirtualMachines(entry.getValue(), null);
		}
		return virtueInstance;
	}

	protected Map<IVmManager, Collection<VirtualMachine>> createVmManagerMappingFromVms(
			Collection<VirtualMachine> vms) {
		HashMap<IVmManager, Collection<VirtualMachine>> map = new HashMap<IVmManager, Collection<VirtualMachine>>();
		for (VirtualMachine vm : vms) {
			IVmManager manager = getVmManagerForVm(vm);
			Collection<VirtualMachine> col = map.get(manager);
			if (col == null) {
				col = new ArrayList<VirtualMachine>();
				map.put(manager, col);
			}
			col.add(vm);
		}
		return map;
	}

	protected Map<IVmManager, Collection<VirtualMachineTemplate>> createVmManagerMappingFromVmTemplates(
			Collection<VirtualMachineTemplate> vmts) {
		HashMap<IVmManager, Collection<VirtualMachineTemplate>> map = new HashMap<IVmManager, Collection<VirtualMachineTemplate>>();
		for (VirtualMachineTemplate vmt : vmts) {
			IVmManager manager = getVmManagerForVmTemplate(vmt);
			Collection<VirtualMachineTemplate> col = map.get(manager);
			if (col == null) {
				col = new ArrayList<VirtualMachineTemplate>();
				map.put(manager, col);
			}
			col.add(vmt);
		}
		return map;
	}

	protected abstract IVmManager getVmManagerForVm(VirtualMachine vm);

	protected abstract IVmManager getVmManagerForVmTemplate(VirtualMachineTemplate vm);
}
