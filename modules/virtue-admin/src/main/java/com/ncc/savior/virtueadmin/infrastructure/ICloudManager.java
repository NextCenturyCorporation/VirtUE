/* 
*  ICloudManager.java
*  
*  VirtUE - Savior Project
*  Created by womitowoju  Dec 20, 2017
*  
*  Copyright (c) 2017 Next Century Corporation. All rights reserved.
*/

package com.ncc.savior.virtueadmin.infrastructure;

import java.util.concurrent.CompletableFuture;

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
	void deleteVirtue(VirtueInstance virtueInstance, CompletableFuture<VirtueInstance> future);

	VirtueInstance createVirtue(VirtueUser user, VirtueTemplate template) throws Exception;

	VirtueInstance startVirtue(VirtueInstance virtueInstance);

	VirtueInstance stopVirtue(VirtueInstance virtueInstance);

}