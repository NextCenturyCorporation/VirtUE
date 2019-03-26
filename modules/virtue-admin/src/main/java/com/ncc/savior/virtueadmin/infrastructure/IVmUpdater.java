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

import java.util.Collection;

import com.ncc.savior.virtueadmin.model.VirtualMachine;

public interface IVmUpdater {

	/**
	 * Adds VMs to the provisioning pipeline. The provisioning pipeline leads the
	 * VMs through a series of tasks each at individual rates. The tasks are:
	 * <ol>
	 * <li>Rename AWS VM
	 * <li>Get networking information from AWS
	 * <li>Test reachability of VM and then add unique RSA key
	 * <li>Start Xpra server
	 * 
	 * @param vms
	 */
	void addVmToProvisionPipeline(Collection<VirtualMachine> vms);

	void addVmsToStartingPipeline(Collection<VirtualMachine> vms);

	void addVmsToStoppingPipeline(Collection<VirtualMachine> vms);

	void addVmsToDeletingPipeline(Collection<VirtualMachine> vms);


}