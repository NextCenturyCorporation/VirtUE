package com.ncc.savior.virtueadmin.model;

import java.util.Collection;
import java.util.Date;

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
	private String loginUser;
	@ManyToMany()
	private Collection<ApplicationDefinition> applications;
	@ColumnDefault("true")
	private boolean enabled;
	private Date lastModification;
	private String lastEditor;

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

	@Override
	public String toString() {
		return "VirtualMachineTemplate [id=" + id + ", name=" + name + ", os=" + os + ", templatePath=" + templatePath
				+ ", loginUser=" + loginUser + ", applications=" + applications + ", enabled=" + enabled
				+ ", lastModification=" + lastModification + ", lastEditor=" + lastEditor + "]";
	}
}
