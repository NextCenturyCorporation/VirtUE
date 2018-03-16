package com.ncc.savior.virtueadmin.model.persistance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;

import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.BaseVirtualMachineTemplate;
import com.ncc.savior.virtueadmin.model.OS;

@Entity
public class JpaVirtualMachineTemplate extends BaseVirtualMachineTemplate {

	@ManyToMany()
	private Collection<ApplicationDefinition> applications;

	public JpaVirtualMachineTemplate(String id, String name, OS os, String templatePath,
			Collection<ApplicationDefinition> applications, String loginUser, boolean enabled, Date lastModification,
			String lastEditor) {
		super(id, name, os, templatePath, loginUser, enabled, lastModification, lastEditor);
		this.applications = applications;
	}

	/**
	 * Used for Jackson deserialization
	 */
	protected JpaVirtualMachineTemplate() {

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

	public JpaVirtualMachineTemplate(BaseVirtualMachineTemplate vmt, Iterable<ApplicationDefinition> japps) {
		super(vmt.getId(), vmt.getName(), vmt.getOs(), vmt.getTemplatePath(), vmt.getLoginUser(), vmt.isEnabled(),
				vmt.getLastModification(), vmt.getLastEditor());
		this.applications = new ArrayList<ApplicationDefinition>();
		Iterator<ApplicationDefinition> itr = japps.iterator();
		while (itr.hasNext()) {
			applications.add(itr.next());
		}
	}

	public Collection<ApplicationDefinition> getApplications() {
		return applications;
	}

	public void setApplications(Collection<ApplicationDefinition> applications) {
		this.applications = applications;
	}

	@Override
	public String toString() {
		return "JpaVirtualMachineTemplate [applications=" + applications + ", id=" + id + ", name=" + name + ", os="
				+ os + ", templatePath=" + templatePath + ", loginUser=" + loginUser + ", enabled=" + enabled
				+ ", lastModification=" + lastModification + ", lastEditor=" + lastEditor + "]";
	}
}
