package com.ncc.savior.virtueadmin.service;

import com.ncc.savior.virtueadmin.data.ITemplateManager;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.VirtueTemplate;

public class AdminService {

	private ITemplateManager templateManager;

	public AdminService(ITemplateManager templateManager) {
		this.templateManager = templateManager;
	}

	public Iterable<VirtueTemplate> getAllVirtueTemplates() {
		return templateManager.getAllVirtueTemplates();
	}

	public Iterable<VirtueTemplate> getAllVmTemplates() {
		return templateManager.getAllVirtueTemplates();
	}

	public Iterable<ApplicationDefinition> getAllApplicationTemplates() {
		return templateManager.getAllApplications();
	}
}
