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
package com.ncc.savior.virtueadmin.infrastructure.staticvm;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.ncc.savior.util.SaviorErrorCode;
import com.ncc.savior.util.SaviorException;
import com.ncc.savior.virtueadmin.cifsproxy.CifsManager;
import com.ncc.savior.virtueadmin.infrastructure.ICloudManager;
import com.ncc.savior.virtueadmin.infrastructure.aws.VirtueCreationAdditionalParameters;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VirtualMachineTemplate;
import com.ncc.savior.virtueadmin.model.VirtueInstance;
import com.ncc.savior.virtueadmin.model.VirtueTemplate;
import com.ncc.savior.virtueadmin.model.VirtueUser;
import com.ncc.savior.virtueadmin.model.VmState;

/**
 * Cloud manager that is backed by a single machine from
 * {@link StaticMachineVmManager}.
 */
public class StaticMachineCloudManager implements ICloudManager {

	private StaticMachineVmManager vmManager;

	public StaticMachineCloudManager(StaticMachineVmManager vmManager) {
		this.vmManager = vmManager;
	}

	@Override
	public void deleteVirtue(VirtueInstance virtueInstance, CompletableFuture<VirtueInstance> future) {
		// do nothing
	}

	@Override
	public VirtueInstance createVirtue(VirtueUser user, VirtueTemplate template) throws Exception {
		Collection<VirtualMachineTemplate> templates = template.getVmTemplates();
		Collection<VirtualMachine> vms = vmManager.provisionVirtualMachineTemplates(user, templates, null,
				new VirtueCreationAdditionalParameters(template.getName()));
		VirtueInstance virtue = new VirtueInstance(UUID.randomUUID().toString(), template.getName(), user.getUsername(),
				template.getId(), template.getColor(), template.getApplications(), vms);
		return virtue;
	}

	@Override
	public VirtueInstance startVirtue(VirtueInstance virtueInstance) {
		// single machine should always be started with this implementation.
		for (VirtualMachine vm : virtueInstance.getVms()) {
			vm.setState(VmState.RUNNING);
		}
		return virtueInstance;
	}

	@Override
	public VirtueInstance stopVirtue(VirtueInstance virtueInstance) {
		// single machine should always be started with this implementation.
		for (VirtualMachine vm : virtueInstance.getVms()) {
			vm.setState(VmState.STOPPED);
		}
		return virtueInstance;
	}

	@Override
	public void rebootVm(VirtualMachine vm, String virtue) {
		// TODO Auto-generated method stub
	}

	@Override
	public void sync(List<String> ids) {
		throw new SaviorException(SaviorErrorCode.NOT_IMPLEMENTED, "Sync not implemented in this implementation");
	}

	@Override
	public void setCifsManager(CifsManager cifsManager) {
		throw new SaviorException(SaviorErrorCode.NOT_IMPLEMENTED, "CIFS not implemented in this implementation");
		
		
	}
}
