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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import com.ncc.savior.virtueadmin.model.Role;
import com.ncc.savior.virtueadmin.model.User;
import com.ncc.savior.virtueadmin.model.Virtue;
import com.ncc.savior.virtueadmin.model.Virtues;
import com.ncc.savior.virtueadmin.service.VirtueUserService;
import com.ncc.savior.virtueadmin.util.WebServiceUtil;

import templates.ResourceConstants;

/*
 * VirtueRestService class exposes api about a virtue. 
 * 
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "virtues")
@Path("/virtues")
public class VirtueRestService {
	
	public VirtueRestService() {
		//TODO dependency injection needed!
		System.out.println("const");		
	}

	// The information below is a temporary in memory data
	private static Map<Integer, Virtue> INMEMORYDB = new HashMap<>();
	static {
		// Virtue 1
		int id = 0x001;
		String username = "Kyle Drumm";
		int roleid = 0x100;
		List<String> applications = new ArrayList<String>();
		applications.add("Microsoft Word");
		applications.add("Gimp");
		applications.add("Firefox");

		List<String> transducers = new ArrayList<String>();
		transducers.add("network");
		transducers.add("app_monitor");

		// Set ip address for the whole of the virtue.

		String ipaddr = "192.168.1.22";

		Virtue v1 = new Virtue(id, username, roleid, applications, transducers, ipaddr);
		INMEMORYDB.put(v1.getId(), v1);

		// Virtue 2
		id = 0x002;
		username = "Kyle Drumm";
		roleid = 0x100;
		applications = new ArrayList<String>();
		applications.add("Microsoft Word");
		applications.add("Gimp");
		applications.add("Firefox");

		transducers = new ArrayList<String>();
		transducers.add("network");
		transducers.add("app_monitor");

		Virtue v2 = new Virtue(id, username, roleid, applications, transducers, ipaddr);
		INMEMORYDB.put(v2.getId(), v2);
	}

	private VirtueUserService userService;

	// This method returns all the virtues for a user with userToken
	@GET
	@Produces("application/json")
	@Path(ResourceConstants.VIRTUE_GETALL_BY_USERTOKEN)
	public Response getAllVirtueByUser(@PathParam("userToken") int userToken) throws URISyntaxException {
		Virtues virtues = new Virtues();
		virtues.setVirtues(new ArrayList<>(INMEMORYDB.values()));

		return Response.status(200).entity(virtues).build();
	}

	/**
	 * Path from API: user role list
	 * 
	 * @param userToken
	 * @return
	 * @throws URISyntaxException
	 */
	@GET
	@Produces("application/json")
	@Path("user/roles")
	public List<Role> getRolesForUser() throws URISyntaxException {
		try {
			User user = getUserFromSecurity();
			List<Role> roles = userService.getRoles(user);
			return roles;
		} catch (RuntimeException e) {
			//TODO fix createWebserviceException
			//Probably need to create our own exception
			//Needs to create ExceptionMapper for jersey.
			throw WebServiceUtil.createWebserviceException(e);
		}
	}

	private User getUserFromSecurity() {
		// TODO hook up to Spring Security at some point.
		return User.testUser();
	}

}
