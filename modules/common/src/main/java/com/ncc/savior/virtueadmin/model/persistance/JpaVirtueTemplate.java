package com.ncc.savior.virtueadmin.model.persistance;

import java.util.Collection;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;

import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.BaseVirtueTemplate;

/**
 * Data Transfer Object (DTO) for templates.
 * 
 *
 */
@Entity
public class JpaVirtueTemplate extends BaseVirtueTemplate {
	@ManyToMany()
	private Collection<JpaVirtualMachineTemplate> vmTemplates;

	// private Set<String> startingResourceIds;
	// private Set<String> startingTransducerIds;

	/**
	 * Used for jackson deserialization
	 * 
	 * @param template
	 * @param templateId
	 */
	public JpaVirtueTemplate(String templateId, JpaVirtueTemplate template) {
		super(templateId, template.getName(), template.getVersion(), template.getAwsTemplateName(),
				template.isEnabled(), template.getLastModification(), template.getLastEditor());
		this.vmTemplates = template.getVmTemplates();
	}

	public JpaVirtueTemplate(String id, String name, String version, Collection<JpaVirtualMachineTemplate> vmTemplates,
			String awsTemplateName, boolean enabled, Date lastModification, String lastEditor) {
		super(id, name, version, awsTemplateName, enabled, lastModification, lastEditor);
		this.vmTemplates = vmTemplates;
	}

	protected JpaVirtueTemplate() {
		super();
	}

	public Set<ApplicationDefinition> getApplications() {
		return getVmTemplates().stream().flatMap(vmTemplate -> vmTemplate.getApplications().parallelStream())
				.collect(Collectors.toSet());
	}

	public Collection<JpaVirtualMachineTemplate> getVmTemplates() {
		return vmTemplates;
	}

	public void setVmTemplates(Set<JpaVirtualMachineTemplate> vmTemplates) {
		this.vmTemplates = vmTemplates;
	}

	@Override
	public String toString() {
		return "JpaVirtueTemplate [vmTemplates=" + vmTemplates + ", id=" + id + ", name=" + name + ", version="
				+ version + ", enabled=" + enabled + ", lastModification=" + lastModification + ", lastEditor="
				+ lastEditor + ", awsTemplateName=" + awsTemplateName + "]";
	}
}
