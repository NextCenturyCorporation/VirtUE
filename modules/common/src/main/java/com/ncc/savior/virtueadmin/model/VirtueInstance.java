/* 
*  Virtue.java
*  
*  VirtUE - Savior Project
*  Created by Wole OMitowoju 11/16/2017
*  
*  Copyright (c) 2017 Next Century Corporation. All rights reserved.
*/

package com.ncc.savior.virtueadmin.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Virtue class models a virtual unit with the user, applications etc.
 * 
 * 
 */
@Entity
public class VirtueInstance {
	@Id
	private String id;
	private String name;
	private String username;
	private String templateId;
	@OneToMany
	private Collection<VirtualMachine> vms;
	// private Set<String> transducers;
	private VirtueState state;
	@ManyToMany
	private Collection<ApplicationDefinition> applications;

	@Transient
	private Collection<String> virtualMachineIds;
	@Transient
	private Collection<String> applicationIds;

	public VirtueInstance(String id, String name, String username, String templateId,
			Collection<ApplicationDefinition> apps, Collection<VirtualMachine> vms) {
		super();
		this.id = id;
		this.name = name;
		this.username = username;
		this.templateId = templateId;
		this.applications = apps;
		this.vms = vms;
		state = getVirtueStateFrom(vms);
	}

	/**
	 * Used for jackson deserialization
	 */
	protected VirtueInstance() {

	}

	public VirtueInstance(VirtueTemplate template, String username) {
		this(UUID.randomUUID().toString(), template.getName(), username, template.getId(),
				getApplicationsFromTemplate(template), new HashSet<VirtualMachine>());
	}

	public VirtueInstance(VirtueTemplate template, String username, Collection<VirtualMachine> vms) {
		this(UUID.randomUUID().toString(), template.getName(), username, template.getId(),
				getApplicationsFromTemplate(template), vms);

	}

	private static Collection<ApplicationDefinition> getApplicationsFromTemplate(VirtueTemplate template) {
		Collection<ApplicationDefinition> list = new HashSet<ApplicationDefinition>();
		for (VirtualMachineTemplate vmTemp : template.getVmTemplates()) {
			list.addAll(vmTemp.getApplications());
		}
		return list;
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

	@JsonIgnore
	public Collection<VirtualMachine> getVms() {
		return vms;
	}

	public VirtueState getState() {
		return getVirtueStateFrom(vms);
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

	@JsonIgnore
	public Collection<ApplicationDefinition> getApplications() {
		return applications;
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

	protected void setVms(Collection<VirtualMachine> vms) {
		this.vms = vms;
		state = getVirtueStateFrom(vms);
	}

	protected void setTemplateId(String templateId) {
		this.templateId = templateId;
	}

	protected void setApplications(Collection<ApplicationDefinition> applications) {
		this.applications = applications;
	}

	public VirtualMachine findVmByApplicationId(String applicationId) {
		for (VirtualMachine vm : vms) {
			if (vm.findApplicationById(applicationId) != null) {
				return vm;
			}
		}
		return null;
	}

	@JsonGetter
	protected Collection<String> getVirtualMachineIds() {
		if (vms != null) {
			virtualMachineIds = new ArrayList<String>();
			for (VirtualMachine vm : vms) {
				virtualMachineIds.add(vm.getId());
			}
		}
		return virtualMachineIds;
	}

	@JsonGetter
	public Collection<String> getApplicationIds() {
		if (applications != null) {
			applicationIds = new ArrayList<String>();
			for (ApplicationDefinition app : applications) {
				applicationIds.add(app.getId());
			}
		}
		return applicationIds;
	}

	protected void setVirtualMachineIds(Collection<String> virtualMachineIds) {
		this.virtualMachineIds = virtualMachineIds;
	}

	protected void setApplicationIds(Collection<String> applicationIds) {
		this.applicationIds = applicationIds;
	}

	private VirtueState getVirtueStateFrom(Collection<VirtualMachine> vms) {
		// TODO this should probably be handled elsewhere
		state = VirtueState.RUNNING;
		boolean creating = false;
		boolean launching = false;
		boolean deleting = false;
		for (VirtualMachine vm : vms) {
			VmState s = vm.getState();
			switch (s) {
			case RUNNING:
				// do nothing
				break;
			case CREATING:
				creating = true;
				break;
			case LAUNCHING:
				creating = true;
				break;
			case DELETING:
				deleting = true;
				break;
			case ERROR:
				setState(null);
				return null;
			case PAUSED:
				break;
			case PAUSING:
				break;
			case RESUMING:
				break;
			case STOPPED:
				break;
			case STOPPING:
				break;
			default:
				break;
			}
		}
		if ((creating || launching) && deleting) {
			return (null);
		}

		if ((creating || launching)) {
			return (VirtueState.LAUNCHING);
		}
		if (deleting) {
			return (VirtueState.DELETING);
		}
		return VirtueState.RUNNING;
	}

}