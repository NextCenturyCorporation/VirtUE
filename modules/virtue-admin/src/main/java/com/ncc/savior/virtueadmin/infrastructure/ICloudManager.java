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