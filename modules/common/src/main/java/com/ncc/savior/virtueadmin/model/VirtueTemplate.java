package com.ncc.savior.virtueadmin.model;

import java.util.List;
import java.util.Map;

/**
 * Data Transfer Object (DTO) for templates.
 * 
 *
 */
public class VirtueTemplate {
	private String id;
	private String name;
	private String version;
	private Map<String, ApplicationDefinition> applications;
	private List<VirtualMachineTemplate> vmTemplates;
	
	private String awsTemplateName; 

	// private Set<String> startingResourceIds;
	// private Set<String> startingTransducerIds;
	
	public VirtueTemplate(String id, String name, String version, Map<String, ApplicationDefinition> applications2,
			List<VirtualMachineTemplate> vmTemplates) {
		super();
		this.id = id;
		this.name = name;
		this.version = version;
		this.applications = applications2;
		this.vmTemplates = vmTemplates;
		this.setAwsTemplateName("");
	}
	
	
	public VirtueTemplate(String id, String name, String version, Map<String, ApplicationDefinition> applications2,
			List<VirtualMachineTemplate> vmTemplates, String awsTemplateName) {
		super();
		this.id = id;
		this.name = name;
		this.version = version;
		this.applications = applications2;
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

	public Map<String, ApplicationDefinition> getApplications() {
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

	protected void setApplications(Map<String, ApplicationDefinition> applications) {
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

	public String getAwsTemplateName() {
		return awsTemplateName;
	}

	public void setAwsTemplateName(String awsTemplateName) {
		this.awsTemplateName = awsTemplateName;
	}

}
