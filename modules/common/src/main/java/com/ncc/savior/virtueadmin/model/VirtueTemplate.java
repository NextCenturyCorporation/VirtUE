package com.ncc.savior.virtueadmin.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Transient;

import org.hibernate.annotations.ColumnDefault;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;

/**
 * Data Transfer Object (DTO) for templates.
 *
 *
 */
@Entity
public class VirtueTemplate {
	@Id
	private String id;
	private String name;
	private String version;
	@ManyToMany()
	private Collection<VirtualMachineTemplate> vmTemplates;
	@ColumnDefault("true")
	private boolean enabled;
	private Date lastModification;
	private String lastEditor;

	private String userCreatedBy;
	private Date timeCreatedAt;

	private String awsTemplateName;

	private String color;

	@Transient
	private Collection<String> virtualMachineTemplateIds;
	// private Set<String> startingResourceIds;
	// private Set<String> startingTransducerIds;

	/**
	 * Used for jackson deserialization
	 *
	 * @param template
	 * @param templateId
	 */
	public VirtueTemplate(String templateId, VirtueTemplate template) {
		super();
		this.id = templateId;
		this.name = template.getName();
		this.version = template.getVersion();
		this.vmTemplates = template.getVmTemplates();
		this.color = template.getColor();
		this.enabled = template.isEnabled();
		this.lastModification = template.getLastModification();
		this.lastEditor = template.getLastEditor();
		this.awsTemplateName = template.getAwsTemplateName();
		this.awsTemplateName = template.getAwsTemplateName();
	}

	public VirtueTemplate(String id, String name, String version, Collection<VirtualMachineTemplate> vmTemplates,
			String awsTemplateName, String color, boolean enabled, Date lastModification, String lastEditor) {
		super();
		this.id = id;
		this.name = name;
		this.version = version;
		this.vmTemplates = vmTemplates;
		this.color = color;
		this.enabled = enabled;
		this.lastModification = lastModification;
		this.lastEditor = lastEditor;
		this.awsTemplateName = awsTemplateName;
	}

	// public VirtueTemplate(String id, String name, String version, Collection<VirtualMachineTemplate> vmTemplates,
	// 		String awsTemplateName, boolean enabled, Date lastModification, String lastEditor) {
	// 	super();
	// 	this.id = id;
	// 	this.name = name;
	// 	this.version = version;
	// 	this.vmTemplates = vmTemplates;
	// 	this.color = "transparent";
	// 	this.enabled = enabled;
	// 	this.lastModification = lastModification;
	// 	this.lastEditor = lastEditor;
	// 	this.awsTemplateName = awsTemplateName;
	// }

	public VirtueTemplate(String id, String name, String version, VirtualMachineTemplate vmTemplate,
			String awsTemplateName, String color, boolean enabled, Date lastModification, String lastEditor) {
		super();
		this.id = id;
		this.name = name;
		this.version = version;
		this.vmTemplates = new ArrayList<VirtualMachineTemplate>();
		vmTemplates.add(vmTemplate);
		this.color = color;
		this.enabled = enabled;
		this.lastModification = lastModification;
		this.lastEditor = lastEditor;
		this.awsTemplateName = awsTemplateName;
	}

	public VirtueTemplate(String id, String name, String version, String awsTemplateName, String color, boolean enabled,
			Date lastModification, String lastEditor, VirtualMachineTemplate... vmTemplates) {
		super();
		this.id = id;
		this.name = name;
		this.version = version;
		this.vmTemplates = new ArrayList<VirtualMachineTemplate>();
		for (VirtualMachineTemplate vmTemplate : vmTemplates) {
			this.vmTemplates.add(vmTemplate);
		}
		this.color = color;
		this.enabled = enabled;
		this.lastModification = lastModification;
		this.lastEditor = lastEditor;
		this.awsTemplateName = awsTemplateName;
	}

	public VirtueTemplate(String id, String name, String version, Collection<VirtualMachineTemplate> vmTemplates,
			String awsTemplateName, String color, boolean enabled, Date lastModification, String lastEditor, String userCreatedBy, Date timeCreatedAt) {
		super();
		this.id = id;
		this.name = name;
		this.version = version;
		this.vmTemplates = vmTemplates;
		this.color = color;
		this.enabled = enabled;
		this.lastModification = lastModification;
		this.lastEditor = lastEditor;
		this.awsTemplateName = awsTemplateName;
		this.userCreatedBy = userCreatedBy;
		this.timeCreatedAt = timeCreatedAt;
	}

	protected VirtueTemplate() {
		super();
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getVersion() {
		return version;
	}

	@JsonIgnore
	public Set<ApplicationDefinition> getApplications() {
		return getVmTemplates().stream().flatMap(vmTemplate -> vmTemplate.getApplications().parallelStream())
				.collect(Collectors.toSet());
	}

	@JsonIgnore
	public Collection<VirtualMachineTemplate> getVmTemplates() {
		return vmTemplates;
	}

	// below setters are used for jackson deserialization.
	public void setId(String id) {
		this.id = id;
	}

	protected void setName(String name) {
		this.name = name;
	}

	protected void setVersion(String version) {
		this.version = version;
	}

	public void setVmTemplates(Collection<VirtualMachineTemplate> vmTemplates) {
		this.vmTemplates = vmTemplates;
	}

	@Override
	public String toString() { // TODO
		return "VirtueTemplate [id=" + id + ", name=" + name + ", verGGsion=" + version + ", vmTemplates=" + vmTemplates
				+ ", enabled=" + enabled + ", lastModification=" + lastModification + ", lastEditor=" + lastEditor
				+ ", awsTemplateName=" + awsTemplateName + "]";
	}

	public String getAwsTemplateName() {
		return awsTemplateName;
	}

	public void setAwsTemplateName(String awsTemplateName) {
		this.awsTemplateName = awsTemplateName;
	}

	public String  getColor() {
		return color;
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

	@JsonGetter
	public Collection<String> getVirtualMachineTemplateIds() {
		if (vmTemplates != null) {
			virtualMachineTemplateIds = new ArrayList<String>();
			for (VirtualMachineTemplate vmt : vmTemplates) {
				virtualMachineTemplateIds.add(vmt.getId());
			}
		}
		return virtualMachineTemplateIds;
	}

	@JsonGetter
	public Collection<String> getApplicationIds() {
		Collection<String> applicationIds = new ArrayList<String>();
		if (vmTemplates != null) {
			for (VirtualMachineTemplate vmt : vmTemplates) {
				applicationIds.addAll(vmt.getApplicationIds());
			}
		}
		return applicationIds;
	}

	@JsonSetter
	public void setVirtualMachineTemplateIds(Collection<String> virtualMachineTemplateIds) {
		this.vmTemplates = null;
		this.virtualMachineTemplateIds = virtualMachineTemplateIds;
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
}
