package com.ncc.savior.virtueadmin.rest;

import java.net.URISyntaxException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.ncc.savior.virtueadmin.model.User;
import com.ncc.savior.virtueadmin.security.UserService;


@Path("/")
public class HelloResource {

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public Response getHello() throws URISyntaxException {
		User user = UserService.getCurrentUser();
		
		return Response.status(200).entity("Hello World "+user.getUsername()).build();
	}
	
	@GET
	@Path("/login")
	@Produces(MediaType.TEXT_PLAIN)
	public Response getLogin() throws URISyntaxException {
		User user = UserService.getCurrentUser();
		
		return Response.status(200).entity("Hello World "+user.getUsername()).build();
	}
}
