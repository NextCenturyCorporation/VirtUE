package com.ncc.savior.virtueadmin.rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.ncc.savior.virtueadmin.model.VirtueUser;
import com.ncc.savior.virtueadmin.security.SecurityUserService;

/**
 * Rest resource designed for testing and login.
 */
@Path("/")
public class HelloResource {
	private static final Logger logger = LoggerFactory.getLogger(HelloResource.class);
	@Autowired
	private SecurityUserService securityService;

	@GET
	@Path("/")
	@Produces(MediaType.TEXT_PLAIN)
	public Response getHello() throws URISyntaxException {
		VirtueUser user = securityService.getCurrentUser();

		return Response.status(200).entity("Hello World " + user.getUsername()).build();
	}

	@GET
	@Path("/login")
	@Produces(MediaType.TEXT_HTML)
	public Response getLogin(@Context HttpServletRequest request) throws URISyntaxException {
		// String csrf = null;
		// Cookie[] cookies = request.getCookies();
		// if (cookies != null) {
		// for (Cookie cookie : cookies) {
		// if (cookie.getName().equals("XSRF-TOKEN")) {
		// csrf = cookie.getValue();
		// }
		// }
		// }
		String filePath = "/templates/login.html";
		InputStream stream = this.getClass().getResourceAsStream(filePath);
		BufferedReader buf = new BufferedReader(new InputStreamReader(stream));
		StringBuffer str = new StringBuffer();
		String line;
		try {
			while ((line = buf.readLine()) != null) {
				// line = line.replaceAll("csrf-token", csrf);
				str.append(line + "\n");
			}
		} catch (IOException e) {
			String msg = "Unable to read html file=" + filePath + ". ";
			logger.error(msg, e);
			return Response.status(400).entity(msg + e.getMessage()).build();

		}
		return Response.status(200).entity(str.toString()).build();
	}

	@GET
	@Path("/error")
	@Produces(MediaType.TEXT_PLAIN)
	public Response getError() throws URISyntaxException {
		VirtueUser user = securityService.getCurrentUser();
		return Response.status(400).entity("Error for " + user.getUsername()).build();
	}

	@GET
	@Path("/logout")
	@Produces(MediaType.TEXT_PLAIN)
	public Response getLogout() throws URISyntaxException {
		VirtueUser user = securityService.getCurrentUser();
		return Response.status(200).entity("logged out " + user.getUsername()).build();
	}
}
