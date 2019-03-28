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
import java.util.LinkedHashSet;
import java.util.Set;

import com.ncc.savior.virtueadmin.model.VirtualMachine;

/**
 * 
 * Abstract class that handles the management of {@link IStateUpdateListener}s.
 * 
 *
 */
public abstract class BaseVmManager implements IVmManager {

	private Set<IUpdateListener<VirtualMachine>> vmUpdateListeners;

	protected BaseVmManager() {
		this.vmUpdateListeners = new LinkedHashSet<IUpdateListener<VirtualMachine>>();
	}

	@Override
	public void addVmUpdateListener(IUpdateListener<VirtualMachine> listener) {
		vmUpdateListeners.add(listener);
	}

	@Override
	public void removeVmUpdateListener(IUpdateListener<VirtualMachine> listener) {
		vmUpdateListeners.remove(listener);
	}

	protected void notifyOnUpdateVm(VirtualMachine vm) {
		ArrayList<VirtualMachine> vms = new ArrayList<VirtualMachine>(1);
		vms.add(vm);
		for (IUpdateListener<VirtualMachine> listener : vmUpdateListeners) {
			listener.updateElements(vms);
		}
	}

	protected void notifyOnUpdateVms(Collection<VirtualMachine> vms) {
		for (IUpdateListener<VirtualMachine> listener : vmUpdateListeners) {
			listener.updateElements(vms);
		}
	}
}
