/* 
*  Virtue.java
*  
*  VirtUE - Savior Project
*  Created by Wole OMitowoju 11/16/2017
*  
*  Copyright (c) 2017 Next Century Corporation. All rights reserved.
*/

package com.ncc.savior.virtueadmin.model;

import java.util.Map;

/*
 * Virtue class models a virtual unit with the user, applications etc. 
 * 
 * 
 */
public class VirtueInstance {

	private String id;
	private String name;
	private String username;
	private String templateid;
	private Map<String, VirtualMachine> vms;
	// private Set<String> transducers;
	private VirtueState state;

	public VirtueInstance(String id, String name, String username, String templateid, Map<String, VirtualMachine> vms,
			VirtueState state) {
		super();
		this.id = id;
		this.name = name;
		this.username = username;
		this.templateid = templateid;
		this.vms = vms;
		this.state = state;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getUsername() {
		return username;
	}

	public String getTemplateid() {
		return templateid;
	}

	public Map<String, VirtualMachine> getVms() {
		return vms;
	}

	public VirtueState getState() {
		return state;
	}

	@Override
	public String toString() {
		return "VirtueInstance [id=" + id + ", name=" + name + ", username=" + username + ", templateid=" + templateid
				+ ", vms=" + vms + ", state=" + state + "]";
	}

	public void setState(VirtueState state) {
		this.state = state;
	}
}
