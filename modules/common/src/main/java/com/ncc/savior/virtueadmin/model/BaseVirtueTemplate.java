package com.ncc.savior.virtueadmin.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.annotations.ColumnDefault;

/**
 * Data Transfer Object (DTO) for templates.
 * 
 *
 */
@Entity
public abstract class BaseVirtueTemplate implements HasId {
	@Id
	protected String id;
	protected String name;
	protected String version;
	@ColumnDefault("true")
	protected boolean enabled;
	protected Date lastModification;
	protected String lastEditor;
	protected String awsTemplateName;

	// private Set<String> startingResourceIds;
	// private Set<String> startingTransducerIds;

	public BaseVirtueTemplate(String id, String name, String version,
			String awsTemplateName, boolean enabled, Date lastModification, String lastEditor) {
		this.id = id;
		this.name = name;
		this.version = version;
		this.enabled = enabled;
		this.lastModification = lastModification;
		this.lastEditor = lastEditor;
		this.awsTemplateName = awsTemplateName;
	}

	protected BaseVirtueTemplate() {
		super();
	}

	@Override
	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getVersion() {
		return version;
	}

	// below setters are used for jackson deserialization.
	protected void setId(String id) {
		this.id = id;
	}

	protected void setName(String name) {
		this.name = name;
	}

	protected void setVersion(String version) {
		this.version = version;
	}

	public String getAwsTemplateName() {
		return awsTemplateName;
	}

	public void setAwsTemplateName(String awsTemplateName) {
		this.awsTemplateName = awsTemplateName;
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
}
