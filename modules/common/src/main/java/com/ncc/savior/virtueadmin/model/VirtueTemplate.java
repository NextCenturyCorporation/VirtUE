package com.ncc.savior.virtueadmin.model;

import java.util.Collection;
import java.util.Date;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;

import org.hibernate.annotations.ColumnDefault;

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

	private String awsTemplateName;

	// private Set<String> startingResourceIds;
	// private Set<String> startingTransducerIds;

	public VirtueTemplate(String templateId, VirtueTemplate template) {
		this(templateId, template.getName(), template.getVersion(), template.getVmTemplates(),
				template.getAwsTemplateName(), template.isEnabled(), template.getLastModification(),
				template.getLastEditor());
	}

	public VirtueTemplate(String id, String name, String version, Collection<VirtualMachineTemplate> vmTemplates,
			String awsTemplateName, boolean enabled, Date lastModification, String lastEditor) {
		super();
		this.id = id;
		this.name = name;
		this.version = version;
		this.vmTemplates = vmTemplates;
		this.enabled = enabled;
		this.lastModification = lastModification;
		this.lastEditor = lastEditor;
		this.awsTemplateName = awsTemplateName;
	}

	public VirtueTemplate(String name, String version, Collection<VirtualMachineTemplate> vmTemplates,
			String awsTemplateName, boolean enabled, Date lastModification, String lastEditor) {
		this(UUID.randomUUID().toString(), name, version, vmTemplates, awsTemplateName, enabled, lastModification,
				lastEditor);
	}
	
	/**
	 * Used for jackson deserialization
	 * 
	 * @param template
	 * @param templateId
	 */
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

	public Set<ApplicationDefinition> getApplications() {
		return getVmTemplates().stream().flatMap(vmTemplate -> vmTemplate.getApplications().parallelStream())
				.collect(Collectors.toSet());
	}

	public Collection<VirtualMachineTemplate> getVmTemplates() {
		return vmTemplates;
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

	public void setVmTemplates(Set<VirtualMachineTemplate> vmTemplates) {
		this.vmTemplates = vmTemplates;
	}

	@Override
	public String toString() {
		return "VirtueTemplate [id=" + id + ", name=" + name + ", version=" + version + ", vmTemplates=" + vmTemplates
				+ ", enabled=" + enabled + ", lastModification=" + lastModification + ", lastEditor=" + lastEditor
				+ ", awsTemplateName=" + awsTemplateName + "]";
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
