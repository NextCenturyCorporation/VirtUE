package com.ncc.savior.virtueadmin.model;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;

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
	private Set<VirtualMachineTemplate> vmTemplates;
	@ManyToMany()
	private Set<UserName> userNames;

	private String awsTemplateName;

	// private Set<String> startingResourceIds;
	// private Set<String> startingTransducerIds;

	public VirtueTemplate(String id, String name, String version, Set<VirtualMachineTemplate> vmTemplates) {
		this(id, name, version, vmTemplates, "");
	}

	public VirtueTemplate(String id, String name, String version, Set<VirtualMachineTemplate> vmTemplates,
			String awsTemplateName) {
		super();
		this.id = id;
		this.name = name;
		this.version = version;
		this.vmTemplates = vmTemplates;
		this.awsTemplateName = awsTemplateName;
	}

	/**
	 * Used for jackson deserialization
	 */
	protected VirtueTemplate() {

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

	public Set<VirtualMachineTemplate> getVmTemplates() {
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
				+ "]";
	}

	public Collection<UserName> retrieveUserNames() {
		return userNames;
	}

	public String getAwsTemplateName() {
		return awsTemplateName;
	}

	public void setAwsTemplateName(String awsTemplateName) {
		this.awsTemplateName = awsTemplateName;
	}

}
