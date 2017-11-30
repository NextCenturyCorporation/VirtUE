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

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.springframework.beans.factory.annotation.Autowired;

import com.ncc.savior.virtueadmin.model.User;
import com.ncc.savior.virtueadmin.model.desktop.DesktopVirtue;
import com.ncc.savior.virtueadmin.model.desktop.DesktopVirtueApplication;
import com.ncc.savior.virtueadmin.service.DesktopVirtueService;
import com.ncc.savior.virtueadmin.util.WebServiceUtil;

/*
 * VirtueRestService class exposes api about a virtue. 
 * 
 */

@Path("/desktop")
public class DesktopRestService {

	@Autowired
	private DesktopVirtueService desktopService;

	// This method returns all the virtues for a user with userToken
	@GET
	@Produces("application/json")
	@Path("virtue")
	public Set<DesktopVirtue> getAllVirtueByUser() throws URISyntaxException {
		try {
			User user = getUserFromSecurity();
			// List<VirtueInstance> virtues = userService.getVirtues(user);
			Set<DesktopVirtue> virtues = desktopService.getDesktopVirtuesForUser(user);
			return virtues;
		} catch (RuntimeException e) {
			// TODO fix createWebserviceException
			// Probably need to create our own exception
			// Needs to create ExceptionMapper for jersey.
			throw WebServiceUtil.createWebserviceException(e);
		}
	}

	@GET
	@Produces("application/json")
	@Path("virtue/{virtueId}/{applicationId}/start")
	public DesktopVirtueApplication startApplication(@PathParam("virtueId") String virtueId,
			@PathParam("applicationId") String applicationId) {
		try {
			User user = getUserFromSecurity();
			return desktopService.startApplication(user, virtueId, applicationId);
		} catch (RuntimeException | IOException e) {
			// TODO fix createWebserviceException
			// Probably need to create our own exception
			// Needs to create ExceptionMapper for jersey.
			throw WebServiceUtil.createWebserviceException(e);
		}
	}

	@GET
	@Produces("application/json")
	@Path("template/{templateId}/{applicationId}/start")
	public DesktopVirtueApplication startApplicationFromTemplate(@PathParam("templateId") String templateId,
			@PathParam("applicationId") String applicationId) {
		try {
			User user = getUserFromSecurity();
			return desktopService.startApplicationFromTemplate(user, templateId, applicationId);
		} catch (RuntimeException | IOException e) {
			// TODO fix createWebserviceException
			// Probably need to create our own exception
			// Needs to create ExceptionMapper for jersey.
			throw WebServiceUtil.createWebserviceException(e);
		}
	}

	private User getUserFromSecurity() {
		// TODO hook up to Spring Security at some point.
		return User.testUser();
	}
}
