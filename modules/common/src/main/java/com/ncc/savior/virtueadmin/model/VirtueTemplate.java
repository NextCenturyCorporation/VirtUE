package com.ncc.savior.virtueadmin.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;

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
	@ManyToMany(fetch = FetchType.EAGER, mappedBy = "VirtueTemplate", cascade = CascadeType.ALL)
	@ElementCollection(targetClass = VirtualMachineTemplate.class)
	private Collection<VirtualMachineTemplate> vmTemplates;
	@LazyCollection(LazyCollectionOption.FALSE)
	@ManyToMany(mappedBy = "UserName", cascade = CascadeType.ALL)
	@ElementCollection(targetClass = UserName.class)
	private Collection<UserName> userNames;

	// private Set<String> startingResourceIds;
	// private Set<String> startingTransducerIds;
	public VirtueTemplate(String id, String name, String version, Collection<VirtualMachineTemplate> vmTemplates) {
		super();
		this.id = id;
		this.name = name;
		this.version = version;
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

	protected void setVmTemplates(List<VirtualMachineTemplate> vmTemplates) {
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

}
