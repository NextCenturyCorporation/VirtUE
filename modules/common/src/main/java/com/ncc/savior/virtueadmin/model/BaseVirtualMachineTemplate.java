package com.ncc.savior.virtueadmin.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.annotations.ColumnDefault;

@Entity
public abstract class BaseVirtualMachineTemplate implements HasId {

	@Id
	protected String id;
	protected String name;
	protected OS os;
	protected String templatePath;
	protected String loginUser;
	@ColumnDefault("true")
	protected boolean enabled;
	protected Date lastModification;
	protected String lastEditor;

	public BaseVirtualMachineTemplate(String id, String name, OS os, String templatePath, String loginUser,
			boolean enabled, Date lastModification, String lastEditor) {
		super();
		this.id = id;
		this.name = name;
		this.os = os;
		this.templatePath = templatePath;
		this.enabled = enabled;
		this.lastModification = lastModification;
		this.lastEditor = lastEditor;
		this.loginUser = loginUser;
	}

	/**
	 * Used for Jackson deserialization
	 */
	protected BaseVirtualMachineTemplate() {

	}

	// public VirtualMachineTemplate(String templateId, VirtualMachineTemplate
	// vmTemplate) {
	// super();
	// this.id = templateId;
	// this.name = vmTemplate.getName();
	// this.os = vmTemplate.getOs();
	// this.templatePath = vmTemplate.getTemplatePath();
	// this.applications = vmTemplate.getApplications();
	// this.enabled = vmTemplate.isEnabled();
	// this.lastModification = vmTemplate.getLastModification();
	// this.lastEditor = vmTemplate.getLastEditor();
	// this.loginUser = vmTemplate.getLoginUser();
	// }

	@Override
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
}
