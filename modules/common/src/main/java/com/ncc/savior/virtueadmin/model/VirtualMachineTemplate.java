package com.ncc.savior.virtueadmin.model;

import java.util.Date;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;

import org.hibernate.annotations.ColumnDefault;

@Entity
public class VirtualMachineTemplate {

	@Id
	private String id;
	private String name;
	private OS os;
	private String templatePath;
	@ManyToMany()
	private Set<ApplicationDefinition> applications;
	@ColumnDefault("true")
	private boolean enabled;
	private Date lastModification;
	private String lastEditor;

	public VirtualMachineTemplate(String id, String name, OS os, String templatePath,
			Set<ApplicationDefinition> applications, boolean enabled, Date lastModification, String lastEditor) {
		super();
		this.id = id;
		this.name = name;
		this.os = os;
		this.templatePath = templatePath;
		this.applications = applications;
		this.enabled = enabled;
		this.lastModification = lastModification;
		this.lastEditor = lastEditor;
	}

	public VirtualMachineTemplate(String name, OS os, String templatePath, Set<ApplicationDefinition> applications,
			boolean enabled, Date lastModification, String lastEditor) {
		this(UUID.randomUUID().toString(), name, os, templatePath, applications, enabled, lastModification, lastEditor);
	}
	
	/**
	 * Used for Jackson deserialization
	 */
	protected VirtualMachineTemplate() {

	}

	public VirtualMachineTemplate(String templateId, VirtualMachineTemplate vmTemplate) {
		this(templateId, vmTemplate.getName(), vmTemplate.getOs(), vmTemplate.getTemplatePath(),
				vmTemplate.getApplications(), vmTemplate.isEnabled(), vmTemplate.getLastModification(),
				vmTemplate.getLastEditor());
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

	public void setApplications(Set<ApplicationDefinition> applications) {
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

	@Override
	public String toString() {
		return "VirtualMachineTemplate [id=" + id + ", name=" + name + ", os=" + os + ", templatePath=" + templatePath
				+ ", applications=" + applications + ", enabled=" + enabled + ", lastModification=" + lastModification
				+ ", lastEditor=" + lastEditor + "]";
	}
}
