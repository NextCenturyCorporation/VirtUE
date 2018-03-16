/* 
*  Virtue.java
*  
*  VirtUE - Savior Project
*  Created by Wole OMitowoju 11/16/2017
*  
*  Copyright (c) 2017 Next Century Corporation. All rights reserved.
*/

package com.ncc.savior.virtueadmin.model;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Virtue class models a virtual unit with the user, applications etc.
 * 
 * 
 */
@Entity
public abstract class BaseVirtueInstance implements HasId {
	@Id
	protected String id;
	protected String name;
	protected String username;
	protected String templateId;
	// private Set<String> transducers;
	protected VirtueState state;

	public BaseVirtueInstance(String id, String name, String username, String templateId) {
		super();
		this.id = id;
		this.name = name;
		this.username = username;
		this.templateId = templateId;
	}

	/**
	 * Used for jackson deserialization
	 */
	protected BaseVirtueInstance() {

	}

	@Override
	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getUsername() {
		return username;
	}

	public void setState(VirtueState state) {
		this.state = state;
	}

	public String getTemplateId() {
		return templateId;
	}

	// below setters are for jackson deserialization
	public void setId(String id) {
		this.id = id;
	}

	protected void setName(String name) {
		this.name = name;
	}

	protected void setUsername(String username) {
		this.username = username;
	}

	protected void setTemplateid(String templateid) {
		this.templateId = templateid;
	}

	protected void setTemplateId(String templateId) {
		this.templateId = templateId;
	}
}
