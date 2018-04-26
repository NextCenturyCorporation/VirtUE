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

	void deleteVirtue(VirtueInstance virtueInstance, CompletableFuture<VirtueInstance> future);

	VirtueInstance createVirtue(VirtueUser user, VirtueTemplate template) throws Exception;

	VirtueInstance startVirtue(VirtueInstance virtueInstance);

	VirtueInstance stopVirtue(VirtueInstance virtueInstance);

}