package com.ncc.savior.virtueadmin.config;

import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.stereotype.Component;

import com.ncc.savior.virtueadmin.rest.VirtueRestService;


@Component
public class ApiConfig extends ResourceConfig {
	
	public ApiConfig() {
		
		register(VirtueRestService.class); 
		
	}

}
