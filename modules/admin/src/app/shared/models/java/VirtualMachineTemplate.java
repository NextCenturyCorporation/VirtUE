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
