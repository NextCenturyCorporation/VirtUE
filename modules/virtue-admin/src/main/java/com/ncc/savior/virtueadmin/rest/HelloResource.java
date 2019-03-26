/*
 * Copyright (C) 2019 Next Century Corporation
 * 
 * This file may be redistributed and/or modified under either the GPL
 * 2.0 or 3-Clause BSD license. In addition, the U.S. Government is
 * granted government purpose rights. For details, see the COPYRIGHT.TXT
 * file at the root of this project.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 * 
 * SPDX-License-Identifier: (GPL-2.0-only OR BSD-3-Clause)
 */
package com.ncc.savior.virtueadmin.rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.websocket.server.PathParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.ncc.savior.virtueadmin.model.VirtueUser;
import com.ncc.savior.virtueadmin.security.SecurityUserService;

import io.swagger.v3.jaxrs2.integration.resources.BaseOpenApiResource;
import io.swagger.v3.oas.annotations.Operation;

/**
 * Rest resource designed for testing and login.
 */
@Path("/")
public class HelloResource extends BaseOpenApiResource {
	private static final Logger logger = LoggerFactory.getLogger(HelloResource.class);
	@Autowired
	private SecurityUserService securityService;

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Operation(hidden = true)
	public Response getHello() throws URISyntaxException {
		VirtueUser user = securityService.getCurrentUser();

		return Response.status(200).entity("Hello World " + user.getUsername()).build();
	}

	@GET
	@Path("/login")
	@Produces(MediaType.TEXT_HTML)
	@Operation(hidden = true)
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

	@POST
	@Path("/login")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.TEXT_HTML)
	@Operation(summary = "Login.", description = "Login endpoint.")
	public Response getLogin(@FormParam("username") String username, @FormParam("password") String password) {
		//This method is bypassed by spring securities login.  It is only here to force Swagger to produce API documentation.
		return null;
	}

	@GET
	@Path("/error")
	@Produces(MediaType.TEXT_PLAIN)
	@Operation(hidden = true)
	public Response getError() throws URISyntaxException {
		VirtueUser user = securityService.getCurrentUser();
		return Response.status(400).entity("Error for " + user.getUsername()).build();
	}

	@GET
	@Path("/logout")
	@Produces(MediaType.TEXT_PLAIN)
	@Operation(summary = "Logout current session.", description = "Logout existing session.")
	public Response getLogout() throws URISyntaxException {
		VirtueUser user = securityService.getCurrentUser();
		return Response.status(200).entity("logged out " + user.getUsername()).build();
	}

	@Context
	ServletConfig config;

	@Context
	Application app;

	@GET
	@Path("/api")
	@Produces({ MediaType.APPLICATION_JSON })
	@Operation(hidden = true)
	public Response getApi(@Context HttpHeaders headers, @Context UriInfo uriInfo) throws Exception {
		return super.getOpenApi(headers, config, app, uriInfo, "json");
	}

	// TODO This is a hack to get the files we need exposed. There is a much better
	// way to just allow the server to bypass them. Also, the PathParam didn't work.
	@GET
	@Path("/api/{file}")
	@Operation(hidden = true)
	public Response getApiUi(@Context HttpHeaders headers, @Context UriInfo uriInfo, @PathParam("file") String file)
			throws Exception {
		String filePath = "/apiui/" + uriInfo.getPathParameters().getFirst("file");

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
		String type = MediaType.TEXT_HTML;
		if (filePath.endsWith(".js")) {
			type = "text/javascript";
		}
		if (filePath.endsWith(".css")) {
			type = "text/css";
		}

		return Response.status(200).type(type).entity(str.toString()).build();
	}
}
