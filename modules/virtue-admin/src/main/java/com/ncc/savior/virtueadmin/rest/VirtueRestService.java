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

import javax.ws.rs.Path;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import com.ncc.savior.virtueadmin.model.Virtue;
import com.ncc.savior.virtueadmin.model.Virtues;


/*
 * VirtueRestService class exposes api about a virtue. 
 * 
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "virtues")
@Path("/virtues")
public class VirtueRestService {
	
	//The information below is a temporary in memory data
	private static Map<Integer, Virtue> INMEMORYDB = new HashMap<>();
	static
	{
		//Virtue 1
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
		
		//Set ip address for the whole of the virtue. 
		
		String ipaddr = "192.168.1.22"; 
		
		Virtue v1 = new Virtue(id, username, roleid, applications, transducers, ipaddr); 
		INMEMORYDB.put(v1.getId(), v1); 
		
		//Virtue 2
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
	
	
	// This method returns all the virtues for a user with userToken
	@GET
	@Produces("application/json")
	@Path("/user/{userToken}")
	public Response getAllVirtueByUser(@PathParam("userToken") int userToken) throws URISyntaxException {
		Virtues virtues = new Virtues(); 	
		virtues.setVirtues(new ArrayList<>(INMEMORYDB.values()));
		
		return Response.status(200).entity(virtues).build();

	}
	
	
	
	

}
