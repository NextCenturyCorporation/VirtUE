package com.ncc.savior.virtueadmin.model;

import java.util.Collection;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;


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
	@LazyCollection(LazyCollectionOption.FALSE)
	@OneToMany(cascade = CascadeType.ALL)
	@ElementCollection(targetClass = ApplicationDefinition.class)
	private Collection<ApplicationDefinition> applications;
	@LazyCollection(LazyCollectionOption.FALSE)
	@OneToMany(cascade = CascadeType.ALL)
	@ElementCollection(targetClass = VirtualMachineTemplate.class)
	private Collection<VirtualMachineTemplate> vmTemplates;

	// private Set<String> startingResourceIds;
	// private Set<String> startingTransducerIds;
	public VirtueTemplate(String id, String name, String version, Collection<ApplicationDefinition> applications,
			Collection<VirtualMachineTemplate> vmTemplates) {
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

	public Collection<ApplicationDefinition> getApplications() {
		return applications;
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

	protected void setApplications(Collection<ApplicationDefinition> applications) {
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
