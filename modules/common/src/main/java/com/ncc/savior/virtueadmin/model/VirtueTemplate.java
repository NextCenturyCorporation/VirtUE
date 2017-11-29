package com.ncc.savior.virtueadmin.model;

import java.util.List;
import java.util.Set;

/**
 * Data Transfer Object (DTO) for templates.
 * 
 *
 */
public class VirtueTemplate {
	private String id;
	private String name;
	private String version;
	private Set<ApplicationDefinition> applications;
	private List<VirtualMachineTemplate> vmTemplates;

	// private Set<String> startingResourceIds;
	// private Set<String> startingTransducerIds;
	public VirtueTemplate(String id, String name, String version, Set<ApplicationDefinition> applications,
			List<VirtualMachineTemplate> vmTemplates) {
		super();
		this.id = id;
		this.name = name;
		this.version = version;
		this.applications = applications;
		this.vmTemplates = vmTemplates;
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
		return applications;
	}

	public List<VirtualMachineTemplate> getVmTemplates() {
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

	protected void setApplications(Set<ApplicationDefinition> applications) {
		this.applications = applications;
	}

	protected void setVmTemplates(List<VirtualMachineTemplate> vmTemplates) {
		this.vmTemplates = vmTemplates;
	}

	@Override
	public String toString() {
		return "VirtueTemplate [id=" + id + ", name=" + name + ", version=" + version + ", applications=" + applications
				+ ", vmTemplates=" + vmTemplates + "]";
	}

}
