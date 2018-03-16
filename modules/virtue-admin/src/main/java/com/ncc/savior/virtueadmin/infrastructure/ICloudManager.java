/* 
*  ICloudManager.java
*  
*  VirtUE - Savior Project
*  Created by womitowoju  Dec 20, 2017
*  
*  Copyright (c) 2017 Next Century Corporation. All rights reserved.
*/

package com.ncc.savior.virtueadmin.infrastructure;

import com.ncc.savior.virtueadmin.model.persistance.JpaVirtueInstance;
import com.ncc.savior.virtueadmin.model.persistance.JpaVirtueTemplate;
import com.ncc.savior.virtueadmin.model.persistance.JpaVirtueUser;

/**
 * Cloud Managers are classes that create, delete, and manage entire virtues.
 *
 */
public interface ICloudManager {

	void deleteVirtue(JpaVirtueInstance virtueInstance);

	JpaVirtueInstance createVirtue(JpaVirtueUser user, JpaVirtueTemplate template) throws Exception;

	JpaVirtueInstance startVirtue(JpaVirtueInstance virtueInstance);

	JpaVirtueInstance stopVirtue(JpaVirtueInstance virtueInstance);

}