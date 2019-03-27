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
package com.ncc.savior.virtueadmin.infrastructure.pipelining;

import java.util.Collection;
import java.util.concurrent.ScheduledExecutorService;

import com.ncc.savior.virtueadmin.model.VirtualMachine;

/**
 * Clears the networking information (hostnames, ip address) for a vm. This is
 * useful when the VM is shut down and the networking information could change
 * later.
 * 
 *
 */
public class NetworkingClearingComponent extends BaseGroupedVmPipelineComponent<VirtualMachine> {

	public NetworkingClearingComponent(ScheduledExecutorService executor) {
		super(executor, true, 100, 500);
	}

	@Override
	protected void onExecute(Collection<PipelineWrapper<VirtualMachine>> wrappers) {
		if (!wrappers.isEmpty()) {
			for (PipelineWrapper<VirtualMachine> wrapper : wrappers) {
				VirtualMachine vm = wrapper.get();
				vm.setHostname(null);
				vm.setIpAddress(null);
				vm.setInternalIpAddress(null);
				vm.setInternalHostname(null);
			}
			doOnSuccess(wrappers);
		}
	}

}
