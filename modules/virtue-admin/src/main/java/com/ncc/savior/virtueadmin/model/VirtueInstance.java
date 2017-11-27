/* 
*  Virtue.java
*  
*  VirtUE - Savior Project
*  Created by Wole OMitowoju 11/16/2017
*  
*  Copyright (c) 2017 Next Century Corporation. All rights reserved.
*/

package com.ncc.savior.virtueadmin.model;

import java.util.Set;

/*
 * Virtue class models a virtual unit with the user, applications etc. 
 * 
 * 
 */
public class VirtueInstance {

	/*
	 * The unique identifier for this Virtue. Format is implementation-specific.
	 * Must be unique across all instances
	 */
	private String id;

	private String username;

	private String templateid;

	private Set<String> applications;

	private Set<String> transducers;

	private VirtueState state;

	private String ipAddress;

	public VirtueInstance(String id, String username, String templateid, Set<String> applications,
			Set<String> transducers,
			String ipAddress) {
		super();
		this.id = id;
		this.username = username;
		this.templateid = templateid;
		this.applications = applications;
		this.transducers = transducers;
		this.ipAddress = ipAddress;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTemplateid() {
		return templateid;
	}

	public void setTemplateid(String templateid) {
		this.templateid = templateid;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public Set<String> getApplications() {
		return applications;
	}

	public void setApplications(Set<String> applications) {
		this.applications = applications;
	}

	public Set<String> getTransducers() {
		return transducers;
	}

	public void setTransducers(Set<String> transducers) {
		this.transducers = transducers;
	}

	public VirtueState getState() {
		return state;
	}

	public void setState(VirtueState state) {
		this.state = state;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

}
