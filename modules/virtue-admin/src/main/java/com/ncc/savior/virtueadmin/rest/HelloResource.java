package com.ncc.savior.virtueadmin.rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URISyntaxException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;

import org.apache.http.HttpRequest;

import com.ncc.savior.virtueadmin.model.VirtueUser;
import com.ncc.savior.virtueadmin.security.UserService;

@Path("/")
public class HelloResource {

	@GET
	@Path("/")
	@Produces(MediaType.TEXT_PLAIN)
	public Response getHello() throws URISyntaxException {
		VirtueUser user = UserService.getCurrentUser();

		return Response.status(200).entity("Hello World " + user.getUsername()).build();
	}

	@GET
	@Path("/login")
	@Produces(MediaType.TEXT_HTML)
	public Response getLogin(@Context HttpServletRequest request) throws URISyntaxException {
		String csrf = null;
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals("XSRF-TOKEN")) {
					csrf = cookie.getValue();
				}
			}
		}
		InputStream stream = this.getClass().getResourceAsStream("/templates/login.html");
		BufferedReader buf = new BufferedReader(new InputStreamReader(stream));
		StringBuffer str = new StringBuffer();
		String line;
		try {
			while ((line = buf.readLine()) != null) {
//				line = line.replaceAll("csrf-token", csrf);
				str.append(line + "\n");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return Response.status(200).entity(str.toString()).build();
	}

	@GET
	@Path("/error")
	@Produces(MediaType.TEXT_PLAIN)
	public Response getError() throws URISyntaxException {
		VirtueUser user = UserService.getCurrentUser();
		return Response.status(400).entity("Error for " + user.getUsername()).build();
	}
	
	@GET
	@Path("/logout")
	@Produces(MediaType.TEXT_PLAIN)
	public Response getLogout() throws URISyntaxException {
		VirtueUser user = UserService.getCurrentUser();
		return Response.status(200).entity("logged out " + user.getUsername()).build();
	}
}
