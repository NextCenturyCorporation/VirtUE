package com.ncc.savior.virtueadmin.model.dto;

import java.util.Collection;
import java.util.Date;

import com.ncc.savior.util.ConversionUtil;
import com.ncc.savior.virtueadmin.model.BaseVirtualMachineTemplate;
import com.ncc.savior.virtueadmin.model.OS;
import com.ncc.savior.virtueadmin.model.persistance.JpaVirtualMachineTemplate;

public class VirtualMachineTemplateDto extends BaseVirtualMachineTemplate {

	private Collection<String> applicationIds;

	public VirtualMachineTemplateDto(String id, String name, OS os, String templatePath,
			Collection<String> applicationIds, String loginUser, boolean enabled, Date lastModification,
			String lastEditor) {
		super(id, name, os, templatePath, loginUser, enabled, lastModification, lastEditor);
		this.applicationIds = applicationIds;
	}

	/**
	 * Used for Jackson deserialization
	 * 
	 * @param collection
	 * @param vmTemplate
	 * @param templateId
	 */
	public VirtualMachineTemplateDto(String templateId, BaseVirtualMachineTemplate vmTemplate,
			Collection<String> applicationIds) {
		this(templateId, vmTemplate.getName(), vmTemplate.getOs(), vmTemplate.getTemplatePath(), null,
				vmTemplate.getLoginUser(), vmTemplate.isEnabled(), vmTemplate.getLastModification(),
				vmTemplate.getLastEditor());
		this.applicationIds = applicationIds;
	}

	// public VirtualMachineTemplate(String templateId, VirtualMachineTemplate
	// vmTemplate) {
	// super();
	// this.id = templateId;
	// this.name = vmTemplate.getName();
	// this.os = vmTemplate.getOs();
	// this.templatePath = vmTemplate.getTemplatePath();
	// this.applications = vmTemplate.getApplications();
	// this.enabled = vmTemplate.isEnabled();
	// this.lastModification = vmTemplate.getLastModification();
	// this.lastEditor = vmTemplate.getLastEditor();
	// this.loginUser = vmTemplate.getLoginUser();
	// }

	public VirtualMachineTemplateDto(JpaVirtualMachineTemplate jpa) {
		this(jpa.getId(), jpa, ConversionUtil.hasIdIterable(jpa.getApplications()));
	}

	@Override
	public String toString() {
		return "RestVirtualMachineTemplate [applicationIds=" + applicationIds + ", id=" + id + ", name=" + name
				+ ", os=" + os + ", templatePath=" + templatePath + ", loginUser=" + loginUser + ", enabled=" + enabled
				+ ", lastModification=" + lastModification + ", lastEditor=" + lastEditor + "]";
	}

	public Collection<String> getApplicationIds() {
		return applicationIds;
	}

	public void setApplicationIds(Collection<String> applicationIds) {
		this.applicationIds = applicationIds;
	}
}
