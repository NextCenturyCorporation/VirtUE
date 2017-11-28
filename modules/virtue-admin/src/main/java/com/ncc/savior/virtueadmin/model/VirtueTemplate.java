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

	@Override
	public String toString() {
		return "VirtueTemplate [id=" + id + ", name=" + name + ", version=" + version + ", applications=" + applications
				+ ", vmTemplates=" + vmTemplates + "]";
	}

}
