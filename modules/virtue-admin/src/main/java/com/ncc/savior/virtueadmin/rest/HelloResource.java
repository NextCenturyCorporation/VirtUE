//
/* 
*  VirtueRestService.java
*  
*  VirtUE - Savior Project
*  Created by Wole OMitowoju 11/16/2017
*  
*  Copyright (c) 2017 Next Century Corporation. All rights reserved.
*/

package com.ncc.savior.virtueadmin.rest;

import java.net.URISyntaxException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


@Path("/")
public class HelloResource {

	public HelloResource() {
		// this.userService = userService;
		System.out.println("const");
	}
	
	// This method returns all the virtues for a user with userToken
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/")
	public Response getHello() throws URISyntaxException {
		
		return Response.status(200).entity("Hello World").build();
	}
}
