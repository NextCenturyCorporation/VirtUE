package com.ncc.savior.virtueadmin.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

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
	private Collection<VirtualMachineTemplate> vmTemplates;
	@ManyToMany()
	private Collection<UserName> userNames;

	private String awsTemplateName;

	// private Set<String> startingResourceIds;
	// private Set<String> startingTransducerIds;

	public VirtueTemplate(String id, String name, String version, Collection<VirtualMachineTemplate> vmTemplates) {
		super();
		this.id = id;
		this.name = name;
		this.version = version;
		this.vmTemplates = vmTemplates;
		this.setAwsTemplateName("");
	}

	public VirtueTemplate(String id, String name, String version, Collection<VirtualMachineTemplate> vmTemplates,
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

	public Collection<ApplicationDefinition> getApplications() {
		Set<ApplicationDefinition> apps = new HashSet<ApplicationDefinition>();
		for (VirtualMachineTemplate temp : getVmTemplates()) {
			apps.addAll(temp.getApplications());
		}
		return apps;
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

	public void setVmTemplates(Collection<VirtualMachineTemplate> hashSet) {
		this.vmTemplates = hashSet;
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
