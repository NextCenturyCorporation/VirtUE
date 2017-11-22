/* 
*  Virtue.java
*  
*  VirtUE - Savior Project
*  Created by Wole OMitowoju 11/16/2017
*  
*  Copyright (c) 2017 Next Century Corporation. All rights reserved.
*/

package com.ncc.savior.virtueadmin.model;


import java.util.List;

/*
 * Virtue class models a virtual unit with the user, applications etc. 
 * 
 * 
 */
public class Virtue {
	
	/*The unique identifier for this Virtue. Format is
	implementation-specific.
	Must be unique across all instances*/
	private int id = 0; 
	
	private String username = "Kyle"; 
	
	private int roleid = 0x100; 
	
	private List<String> applications; 
	
	private List<String> transducers; 
	
	//private enum state; 

	private String ipAddress ="0.0.0.0";
	

	public Virtue(int id, String username, int roleid, List<String> applications, List<String> transducers,
			String ipAddress) {
		super();
		this.id = id;
		this.username = username;
		this.roleid = roleid;
		this.applications = applications;
		this.transducers = transducers;
		this.ipAddress = ipAddress;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getRoleid() {
		return roleid;
	}

	public void setRoleid(int roleid) {
		this.roleid = roleid;
	}

	public List<String> getApplicationid() {
		return applications;
	}

	public void setApplicationid(List<String> applications) {
		this.applications = applications;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public List<String> getTransducersid() {
		return transducers;
	}

	public void setTransducersid(List<String> transducers) {
		this.transducers = transducers;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	} 
	
	
	
	

}
