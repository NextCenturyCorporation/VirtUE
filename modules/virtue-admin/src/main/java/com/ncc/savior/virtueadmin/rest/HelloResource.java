package com.ncc.savior.virtueadmin.rest;

import java.net.URISyntaxException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


@Path("/")
public class HelloResource {

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/")
	public Response getHello() throws URISyntaxException {
		return Response.status(200).entity("Hello World").build();
	}
}
