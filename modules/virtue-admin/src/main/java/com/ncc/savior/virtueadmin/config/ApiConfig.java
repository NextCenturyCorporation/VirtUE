/* 
*  ApiConfig.java
*  
*  VirtUE - Savior Project
*  Created by Wole OMitowoju 11/16/2017
*  
*  Copyright (c) 2017 Next Century Corporation. All rights reserved.
*/

package com.ncc.savior.virtueadmin.config;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.stereotype.Component;

import com.ncc.savior.virtueadmin.rest.AdminResource;
import com.ncc.savior.virtueadmin.rest.DataResource;
import com.ncc.savior.virtueadmin.rest.DesktopRestService;
import com.ncc.savior.virtueadmin.rest.HelloResource;
import com.ncc.savior.virtueadmin.rest.UserResource;
import com.ncc.savior.virtueadmin.util.WebServiceUtil;

/**
 * ApiConfig is responsible for registering all the webserivce apis.
 * 
 * 
 */
@Component
@ApplicationPath("/")
@PropertySources({ @PropertySource(value = "classpath:savior-server.properties", ignoreResourceNotFound = true), 
		@PropertySource(value = "file:savior-server.properties", ignoreResourceNotFound = true) })
public class ApiConfig extends ResourceConfig {

	public ApiConfig() {

		//register(ContextInitializer.class);
		
		/* Register all you webservice class here: */
		register(DesktopRestService.class);
		register(DataResource.class);
		register(HelloResource.class);
		register(AdminResource.class);
		register(UserResource.class);
		register(WebServiceUtil.class);
		register(MultiPartFeature.class);
		
		register(CorsFilter.class);
		
		property(ServletProperties.FILTER_FORWARD_ON_404, true);
		

	}

}
