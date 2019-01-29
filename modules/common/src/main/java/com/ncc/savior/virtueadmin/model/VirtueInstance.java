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
import java.util.Set;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Virtue class models a virtual unit with the user, applications etc.
 *
 *
 */
@Entity
@Schema(description="")
public class VirtueInstance {
	@Id
	private String id;
	private String name;
	private String username;
	private String templateId;
	private String color;
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

	public VirtueInstance(String id, String name, String username, String templateId, String color,
			Collection<ApplicationDefinition> apps, Collection<VirtualMachine> vms) {
		super();
		this.id = id;
		this.name = name;
		this.username = username;
		this.templateId = templateId;
		this.color = color;
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
		this(UUID.randomUUID().toString(), template.getName(), username, template.getId(), template.getColor(),
				getApplicationsFromTemplate(template), new HashSet<VirtualMachine>());
	}

	public VirtueInstance(VirtueTemplate template, String username, Collection<VirtualMachine> vms) {
		this(UUID.randomUUID().toString(), template.getName(), username, template.getId(), template.getColor(),
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
		this.state = getVirtueStateFrom(vms);
		return state;
	}

	@Override
	public String toString() {
		return "VirtueInstance [id=" + id + ", name=" + name + ", username=" + username + ", templateid=" + templateId
				+ ", color=" + color + ", vms=" + vms + ", state=" + getState() + "]";
	}

	protected void setState(VirtueState state) {
		this.state = state;
	}

	public String getTemplateId() {
		return templateId;
	}

	public String getColor() {
		return color;
	}

	@JsonIgnore
	public Collection<ApplicationDefinition> getApplications() {
		return applications;
	}

	// below setters are for jackson deserialization
	public void setId(String id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	protected void setUsername(String username) {
		this.username = username;
	}

	protected void setTemplateid(String templateid) {
		this.templateId = templateid;
	}

	protected void setColor(String color) {
		this.color = color;
	}

	public void setVms(Collection<VirtualMachine> vms) {
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
		// TODO this should probably be handled elsewhere or rethought
		if (vms == null) {
			return VirtueState.ERROR;
		}
		Set<VirtueState> states = new HashSet<VirtueState>();
		for (VirtualMachine vm : vms) {
			states.add(getVirtueStateFromVmState(vm.getState()));
		}
		// Since states is a set, if there is only one state, it will be returned
		// properly
		if (states.size() == 1) {
			return states.iterator().next();
		}
		if (states.isEmpty() || states.contains(VirtueState.ERROR)) {
			return VirtueState.ERROR;
		}
		// At this point, picking a virtue state gets very tricky unless it falls in one
		// of the "good-path", expected situations where some VM's are in progress
		// towards a state and some are in that state.
		if (states.contains(VirtueState.LAUNCHING) && states.contains(VirtueState.RUNNING)) {
			return VirtueState.LAUNCHING;
		}
		if (states.contains(VirtueState.CREATING)
				&& (states.contains(VirtueState.RUNNING) || states.contains(VirtueState.LAUNCHING))) {
			return VirtueState.CREATING;
		}
		if (states.contains(VirtueState.STOPPING) && states.contains(VirtueState.STOPPED)) {
			return VirtueState.STOPPING;
		}
		if (states.contains(VirtueState.DELETING) && states.contains(VirtueState.DELETED)) {
			return VirtueState.STOPPING;
		}
		return VirtueState.ERROR;
	}

	public static VirtueState getVirtueStateFromVmState(VmState s) {
		switch (s) {
		case RUNNING:
			return VirtueState.RUNNING;
		case CREATING:
			return VirtueState.CREATING;
		case LAUNCHING:
			return VirtueState.LAUNCHING;
		case DELETING:
			return VirtueState.DELETING;
		case DELETED:
			return VirtueState.DELETED;
		case ERROR:
			return VirtueState.ERROR;
		case PAUSED:
			return VirtueState.PAUSED;
		case PAUSING:
			return VirtueState.PAUSING;
		case RESUMING:
			return VirtueState.RESUMING;
		case STOPPED:
			return VirtueState.STOPPED;
		case STOPPING:
			return VirtueState.STOPPING;
		default:
			break;
		}
		return null;
	}

}
