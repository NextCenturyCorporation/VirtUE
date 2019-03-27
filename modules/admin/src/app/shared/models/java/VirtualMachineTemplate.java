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

import java.util.Collection;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;

@Entity
public class VirtualMachineTemplate {

	@Id
	private String id;
	private String name;
	private OS os;
	private String templatePath;
	@ManyToMany()
	private Collection<ApplicationDefinition> applications;

	public VirtualMachineTemplate(String id, String name, OS os, String templatePath,
			Collection<ApplicationDefinition> applications) {
		super();
		this.id = id;
		this.name = name;
		this.os = os;
		this.templatePath = templatePath;
		this.applications = applications;
	}

	/**
	 * Used for Jackson deserialization
	 */
	protected VirtualMachineTemplate() {

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

	public Collection<ApplicationDefinition> getApplications() {
		return applications;
	}

	protected void setId(String id) {
		this.id = id;
	}

	protected void setName(String name) {
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

	@Override
	public String toString() {
		return "VirtualMachineTemplate [id=" + id + ", name=" + name + ", os=" + os + ", templatePath=" + templatePath
				+ ", applications=" + applications + "]";
	}
}
