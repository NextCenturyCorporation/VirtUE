/*
 * Copyright (C) 2019 Next Century Corporation
 * 
 * This file may be redistributed and/or modified under either the GPL
 * 2.0 or 3-Clause BSD license. In addition, the U.S. Government is
 * granted government purpose rights. For details, see the COPYRIGHT.TXT
 * file at the root of this project.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 * 
 * SPDX-License-Identifier: (GPL-2.0-only OR BSD-3-Clause)
 */
/* 
*  Virtue.java
*  
*  VirtUE - Savior Project
*  Created by Wole OMitowoju 11/16/2017
*  
*  Copyright (c) 2017 Next Century Corporation. All rights reserved.
*/

package com.ncc.savior.virtueadmin.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;

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

	public VirtueInstance(String id, String name, String username, String templateId,
			Collection<ApplicationDefinition> apps, Collection<VirtualMachine> vms) {
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

	public Collection<VirtualMachine> getVms() {
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

}
