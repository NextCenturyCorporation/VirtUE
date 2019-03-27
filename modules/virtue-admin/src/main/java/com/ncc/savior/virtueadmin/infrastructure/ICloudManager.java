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
/* 
*  ICloudManager.java
*  
*  VirtUE - Savior Project
*  Created by womitowoju  Dec 20, 2017
*  
*  Copyright (c) 2017 Next Century Corporation. All rights reserved.
*/

package com.ncc.savior.virtueadmin.infrastructure;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.ncc.savior.virtueadmin.cifsproxy.CifsManager;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VirtueInstance;
import com.ncc.savior.virtueadmin.model.VirtueTemplate;
import com.ncc.savior.virtueadmin.model.VirtueUser;

/**
 * Cloud Managers are classes that create, delete, and manage entire virtues.
 *
 */
public interface ICloudManager {

	// It is somewhat odd that there is a future here, but no where else at this
	// level. This is due to the notifiers that update the vms and virtues not
	// having a delete function. We may want to add a delete function to the
	// notifiers and remove the future here. This would contain all futures inside
	// the ICloudManager level.
	/**
	 * Starts the process of deleting a virtue. Implementations should return
	 * quickly and complete the provided future when the task has completed.
	 * 
	 * @param virtueInstance
	 * @param future
	 */
	void deleteVirtue(VirtueInstance virtueInstance, CompletableFuture<VirtueInstance> future);

	/**
	 * Starts the process of creating a {@link VirtueInstance} from a
	 * {@link VirtueTemplate}. The virtue return will not necessary be provisioned,
	 * but instead callers should check the status.
	 * 
	 * @param user
	 * @param template
	 * @return
	 * @throws Exception
	 */
	VirtueInstance createVirtue(VirtueUser user, VirtueTemplate template) throws Exception;

	/**
	 * Starts the process of starting a {@link VirtueInstance}. The
	 * {@link VirtueInstance} returned will not necessary be finished starting.
	 * Callers should check the status.
	 * 
	 * @param virtueInstance
	 * @return
	 */
	VirtueInstance startVirtue(VirtueInstance virtueInstance);

	/**
	 * Starts the process of stopping a {@link VirtueInstance}. The
	 * {@link VirtueInstance} returned will not necessary be finished stopping.
	 * Callers should check the status.
	 * 
	 * @param virtueInstance
	 * @return
	 */
	VirtueInstance stopVirtue(VirtueInstance virtueInstance);
	
	void rebootVm(VirtualMachine vm, String virtueId);

	void sync(List<String> ids);

	void setCifsManager(CifsManager cifsManager);

}