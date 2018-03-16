package com.ncc.savior.virtueadmin.model.dto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import javax.persistence.Entity;

import com.ncc.savior.virtueadmin.model.BaseVirtueTemplate;
import com.ncc.savior.virtueadmin.model.HasId;
import com.ncc.savior.virtueadmin.model.persistance.JpaVirtueTemplate;

/**
 * Data Transfer Object (DTO) for templates.
 * 
 *
 */
@Entity
public class VirtueTemplateDto extends BaseVirtueTemplate {
	private Collection<String> vmTemplateIds;

	// private Set<String> startingResourceIds;
	// private Set<String> startingTransducerIds;

	/**
	 * Used for jackson deserialization
	 * 
	 * @param template
	 * @param templateId
	 */
	public VirtueTemplateDto(String templateId, BaseVirtueTemplate template, Collection<String> vmTemplateIds) {
		super(templateId, template.getName(), template.getVersion(), template.getAwsTemplateName(),
				template.isEnabled(), template.getLastModification(), template.getLastEditor());
		this.vmTemplateIds = vmTemplateIds;
	}

	public VirtueTemplateDto(String id, String name, String version, Collection<String> vmTemplateIds,
			String awsTemplateName, boolean enabled, Date lastModification, String lastEditor) {
		super(id, name, version, awsTemplateName, enabled, lastModification, lastEditor);
		this.vmTemplateIds = vmTemplateIds;
	}

	protected VirtueTemplateDto() {
		super();
	}


	public VirtueTemplateDto(JpaVirtueTemplate jpaTemplate) {
		this(jpaTemplate.getId(), jpaTemplate, vmTemplatesToIds(jpaTemplate.getVmTemplates()));
	}

	private static Collection<String> vmTemplatesToIds(Collection<? extends HasId> hasIds) {
		ArrayList<String> ids = new ArrayList<String>();
		for (HasId hasId : hasIds) {
			String id = hasId.getId();
			ids.add(id);
		}
		return ids;
	}

	public Collection<String> getVmTemplateIds() {
		return vmTemplateIds;
	}

	public void setVmTemplateIds(Collection<String> vmTemplateIds) {
		this.vmTemplateIds = vmTemplateIds;
	}

	@Override
	public String toString() {
		return "RestVirtueTemplate [vmTemplateIds=" + vmTemplateIds + ", id=" + id + ", name=" + name + ", version="
				+ version + ", enabled=" + enabled + ", lastModification=" + lastModification + ", lastEditor="
				+ lastEditor + ", awsTemplateName=" + awsTemplateName + "]";
	}
}
