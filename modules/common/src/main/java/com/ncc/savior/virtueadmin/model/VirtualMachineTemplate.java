package com.ncc.savior.virtueadmin.model;

import java.util.Set;

public class VirtualMachineTemplate {
	private String id;
	private String name;
	private OS os;
	private String templatePath;
	private Set<ApplicationDefinition> applications;

	public VirtualMachineTemplate(String id, String name, OS os, String templatePath,
			Set<ApplicationDefinition> applications) {
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

	public Set<ApplicationDefinition> getApplications() {
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

	protected void setApplications(Set<ApplicationDefinition> applications) {
		this.applications = applications;
	}

	@Override
	public String toString() {
		return "VirtualMachineTemplate [id=" + id + ", name=" + name + ", os=" + os + ", templatePath=" + templatePath
				+ ", applications=" + applications + "]";
	}
}
