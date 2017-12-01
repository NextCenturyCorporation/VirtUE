/* 
*  Virtue.java
*  
*  VirtUE - Savior Project
*  Created by Wole OMitowoju 11/16/2017
*  
*  Copyright (c) 2017 Next Century Corporation. All rights reserved.
*/

package com.ncc.savior.virtueadmin.model;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/*
 * Virtue class models a virtual unit with the user, applications etc. 
 * 
 * 
 */
public class VirtueInstance {

	private String id;
	private String name;
	private String username;
	private String templateId;
	private Map<String, VirtualMachine> vms;
	// private Set<String> transducers;
	private VirtueState state;
	private Map<String, ApplicationDefinition> applications;

	public VirtueInstance(String id, String name, String username, String templateId,
			Map<String, ApplicationDefinition> apps,
			Map<String, VirtualMachine> vms) {
		super();
		this.id = id;
		this.name = name;
		this.username = username;
		this.templateId = templateId;
		this.applications = apps;
		this.vms = vms;
	}

	/**
	 * Used for jackson deserialization
	 * 
	 * @param template
	 */
	protected VirtueInstance() {

	}

	public VirtueInstance(VirtueTemplate template, String username) {
		this(UUID.randomUUID().toString(), template.getName(), username, template.getId(),
				getApplicationsFromTemplate(template), new HashMap<String, VirtualMachine>());

	}

	public VirtueInstance(VirtueTemplate template, String username, Map<String, VirtualMachine> vms) {
		this(UUID.randomUUID().toString(), template.getName(), username, template.getId(),
				getApplicationsFromTemplate(template), vms);

	}

	private static Map<String, ApplicationDefinition> getApplicationsFromTemplate(VirtueTemplate template) {
		Map<String, ApplicationDefinition> map = new HashMap<String, ApplicationDefinition>();
		for (VirtualMachineTemplate vmTemp : template.getVmTemplates()) {
			map.putAll(vmTemp.getApplications());
		}
		return map;
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

	public Map<String, VirtualMachine> getVms() {
		return vms;
	}

	public VirtueState getState() {
		return state;
	}

	@Override
	public String toString() {
		return "VirtueInstance [id=" + id + ", name=" + name + ", username=" + username + ", templateid=" + templateId
				+ ", vms=" + vms + ", state=" + state + "]";
	}

	public void setState(VirtueState state) {
		this.state = state;
	}

	public String getTemplateId() {
		return templateId;
	}

	public Map<String, ApplicationDefinition> getApplications() {
		return applications;
	}

	// below setters are for jackson deserialization
	protected void setId(String id) {
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

	protected void setVms(Map<String, VirtualMachine> vms) {
		this.vms = vms;
	}

	protected void setTemplateId(String templateId) {
		this.templateId = templateId;
	}

	protected void setApplications(Map<String, ApplicationDefinition> applications) {
		this.applications = applications;
	}

	public VirtualMachine findVmByApplicationId(String applicationId) {
		for (VirtualMachine vm :vms.values()) {
			if (vm.getApplications().containsKey(applicationId)) {
				return vm;
			}
		}
		return null;
	}

}
