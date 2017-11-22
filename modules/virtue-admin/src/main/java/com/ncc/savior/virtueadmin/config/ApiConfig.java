/* 
*  ApiConfig.java
*  
*  VirtUE - Savior Project
*  Created by Wole OMitowoju 11/16/2017
*  
*  Copyright (c) 2017 Next Century Corporation. All rights reserved.
*/

package com.ncc.savior.virtueadmin.config;

import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.stereotype.Component;

import com.ncc.savior.virtueadmin.rest.VirtueRestService;
import com.ncc.savior.virtueadmin.util.WebServiceUtil;


/*
 * ApiConfig is responsible for registering all the webserivce 
 * apis. 
 * 
 * 
 */
@Component
public class ApiConfig extends ResourceConfig {
	
	public ApiConfig() {
		
		/*Register all you webservice class here:*/
		register(VirtueRestService.class); 
		
		register(WebServiceUtil.class);
		
	}

}
