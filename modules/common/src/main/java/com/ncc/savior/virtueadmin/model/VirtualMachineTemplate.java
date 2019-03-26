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
package com.ncc.savior.virtueadmin.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Transient;

import org.hibernate.annotations.ColumnDefault;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;

import io.swagger.v3.oas.annotations.media.Schema;

@Entity
@Schema(description = "Object to describe a virtual machine template.  This is just metadata and does not include the image directly.")
public class VirtualMachineTemplate {

	@Id
	@Schema(description = "ID of the virtual machine template.  Typically set by the system.")
	private String id;
	@Schema(description = "Name of the virtual machine template.")
	private String name;
	@Schema(description = "Operating system that the virtual machine template runs.")
	private OS os;
	@Schema(description = "Infrastructure specific path to the virtual machine template image. This value can be different depending on whether the virtual machine should be run directly in AWS (template path is AMI) or run on Xen (template path is path in S3 to image).")
	private String templatePath;
	@Schema(description = "User to be used to login as.")
	private String loginUser;
	@ManyToMany()
	private Collection<ApplicationDefinition> applications;
	@ColumnDefault("true")
	@Schema(description = "Toggle for enabling or diabling the virtual machine.  May not be implemented.")
	private boolean enabled;
	@Schema(description = "Date of last modification for virtual machine template metadata.")
	private Date lastModification;
	@Schema(description = "Username of last editor of this virtual machine template metadata.")
	private String lastEditor;

	@Transient
	@Schema(description = "The list of application IDs that this template provides for the users.")
	private Collection<String> applicationIds;
	@Schema(description = "Security tag used by Xen (linux VM's only) to determine how to setup the firewall.  Options: god, power, TBD")
	private String securityTag;
	@Schema(description = "User who originally created this virtual machine template.")
	private String userCreatedBy;
	@Schema(description = "Time at which this virtual machine was originally created.")
	private Date timeCreatedAt;

	public VirtualMachineTemplate(String id, String name, OS os, String templatePath,
			Collection<ApplicationDefinition> applications, String loginUser, boolean enabled, Date lastModification,
			String lastEditor) {
		super();
		this.id = id;
		this.name = name;
		this.os = os;
		this.templatePath = templatePath;
		this.applications = applications;
		this.enabled = enabled;
		this.lastModification = lastModification;
		this.lastEditor = lastEditor;
		this.loginUser = loginUser;
	}

	public VirtualMachineTemplate(String id, String name, OS os, String templatePath,
			Collection<ApplicationDefinition> applications, String loginUser, boolean enabled, Date lastModification,
			String lastEditor, String userCreatedBy, Date timeCreatedAt) {
		super();
		this.id = id;
		this.name = name;
		this.os = os;
		this.templatePath = templatePath;
		this.applications = applications;
		this.enabled = enabled;
		this.lastModification = lastModification;
		this.lastEditor = lastEditor;
		this.loginUser = loginUser;
		this.userCreatedBy = userCreatedBy;
		this.timeCreatedAt = timeCreatedAt;
	}

	/**
	 * Used for Jackson deserialization
	 */
	protected VirtualMachineTemplate() {

	}

	public VirtualMachineTemplate(String templateId, VirtualMachineTemplate vmTemplate) {
		super();
		this.id = templateId;
		this.name = vmTemplate.getName();
		this.os = vmTemplate.getOs();
		this.templatePath = vmTemplate.getTemplatePath();
		this.applications = vmTemplate.getApplications();
		this.enabled = vmTemplate.isEnabled();
		this.lastModification = vmTemplate.getLastModification();
		this.lastEditor = vmTemplate.getLastEditor();
		this.loginUser = vmTemplate.getLoginUser();
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public OS getOs() {
		return os;
	}

	public String getTemplatePath() {
		return templatePath;
	}

	@JsonIgnore
	public Collection<ApplicationDefinition> getApplications() {
		return applications;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	protected void setOs(OS os) {
		this.os = os;
	}

	protected void setTemplatePath(String templatePath) {
		this.templatePath = templatePath;
	}

	public void setApplications(Collection<ApplicationDefinition> applications) {
		this.applications = applications;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public Date getLastModification() {
		return lastModification;
	}

	public void setLastModification(Date lastModification) {
		this.lastModification = lastModification;
	}

	public String getLastEditor() {
		return lastEditor;
	}

	public void setLastEditor(String lastEditor) {
		this.lastEditor = lastEditor;
	}

	public String getLoginUser() {
		return loginUser;
	}

	public void setLoginUser(String loginUser) {
		this.loginUser = loginUser;
	}

	@JsonSetter
	public void setApplicationIds(Collection<String> appIds) {
		applications = null;
		applicationIds = appIds;
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

	@Override
	public String toString() {
		return "VirtualMachineTemplate [id=" + id + ", name=" + name + ", os=" + os + ", templatePath=" + templatePath
				+ ", loginUser=" + loginUser + ", applications=" + applications + ", enabled=" + enabled
				+ ", lastModification=" + lastModification + ", lastEditor=" + lastEditor + "]";
	}

	public String getSecurityTag() {
		return this.securityTag;
	}

	public void setSecurityTag(String securityTag) {
		this.securityTag = securityTag;
	}

	public Date getTimeCreatedAt() {
		return timeCreatedAt;
	}

	public void setTimeCreatedAt(Date timeCreatedAt) {
		this.timeCreatedAt = timeCreatedAt;
	}

	public String getUserCreatedBy() {
		return userCreatedBy;
	}

	public void setUserCreatedBy(String userCreatedBy) {
		this.userCreatedBy = userCreatedBy;
	}

	public static final Comparator<? super VirtualMachineTemplate> CASE_INSENSITIVE_NAME_COMPARATOR = new CaseInsensitiveNameComparator();

	private static class CaseInsensitiveNameComparator implements Comparator<VirtualMachineTemplate> {
		@Override
		public int compare(VirtualMachineTemplate o1, VirtualMachineTemplate o2) {
			return String.CASE_INSENSITIVE_ORDER.compare(o1.getName(), o2.getName());
		}
	}
}
