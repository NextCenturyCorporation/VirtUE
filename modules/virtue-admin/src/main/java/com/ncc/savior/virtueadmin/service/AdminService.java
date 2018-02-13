package com.ncc.savior.virtueadmin.service;

import com.ncc.savior.virtueadmin.data.ITemplateManager;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.VirtualMachineTemplate;
import com.ncc.savior.virtueadmin.model.VirtueInstance;
import com.ncc.savior.virtueadmin.model.VirtueTemplate;
import com.ncc.savior.virtueadmin.virtue.IActiveVirtueManager;

public class AdminService {

	private IActiveVirtueManager virtueManager;
	private ITemplateManager templateManager;

	public AdminService(IActiveVirtueManager virtueManager, ITemplateManager templateManager) {
		super();
		this.virtueManager = virtueManager;
		this.templateManager = templateManager;
	}

	public AdminService(ITemplateManager templateManager) {
		this.templateManager = templateManager;
	}

	public Iterable<VirtueTemplate> getAllVirtueTemplates() {
		return templateManager.getAllVirtueTemplates();
	}

	public Iterable<VirtualMachineTemplate> getAllVmTemplates() {
		return templateManager.getAllVirtualMachineTemplates();
	}

	public Iterable<ApplicationDefinition> getAllApplicationTemplates() {
		return templateManager.getAllApplications();
	}

	public Iterable<VirtueInstance> getAllActiveVirtues() {
		return virtueManager.getAllActiveVirtues();
	}
}
