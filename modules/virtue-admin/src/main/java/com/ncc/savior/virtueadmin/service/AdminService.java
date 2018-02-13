package com.ncc.savior.virtueadmin.service;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import com.ncc.savior.virtueadmin.data.ITemplateManager;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.User;
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

	public Iterable<VirtueTemplate> getAllVirtueTemplates(User user) {
		return templateManager.getAllVirtueTemplates();
	}

	public Iterable<VirtualMachineTemplate> getAllVmTemplates(User user) {
		return templateManager.getAllVirtualMachineTemplates();
	}

	public Iterable<ApplicationDefinition> getAllApplicationTemplates(User user) {
		return templateManager.getAllApplications();
	}

	public Iterable<VirtueInstance> getAllActiveVirtues(User user) {
		return virtueManager.getAllActiveVirtues();
	}

	public VirtueTemplate getVirtueTemplate(User user, String templateId) {
		Optional<VirtueTemplate> opt = templateManager.getVirtueTemplate(templateId);
		return opt.isPresent() ? opt.get() : null;
	}

	public VirtualMachineTemplate getVmTemplate(User user, String templateId) {
		Optional<VirtualMachineTemplate> opt = templateManager.getVmTemplate(templateId);
		return opt.isPresent() ? opt.get() : null;
	}

	public VirtueInstance getActiveVirtue(User user, String virtueId) {
		return virtueManager.getActiveVirtue(virtueId);
	}

	public ApplicationDefinition getApplicationDefinition(User user, String templateId) {
		Optional<ApplicationDefinition> opt = templateManager.getApplicationDefinition(templateId);
		return opt.isPresent() ? opt.get() : null;
	}

	public VirtueTemplate createNewVirtueTemplate(User user, VirtueTemplate template) {
		String id = UUID.randomUUID().toString();
		return updateVirtueTemplate(user, id, template);
	}

	public ApplicationDefinition createNewApplicationDefinition(User user, ApplicationDefinition appDef) {
		String id = UUID.randomUUID().toString();
		return updateApplicationDefinitions(user, id, appDef);
	}

	public VirtualMachineTemplate createVmTemplate(User user, VirtualMachineTemplate vmTemplate) {
		String id = UUID.randomUUID().toString();
		return updateVmTemplate(user, id, vmTemplate);
	}

	public ApplicationDefinition updateApplicationDefinitions(User user, String templateId,
			ApplicationDefinition appDef) {
		if (!templateId.equals(appDef.getId())) {
			appDef = new ApplicationDefinition(templateId, appDef);
		}
		templateManager.addApplicationDefinition(appDef);
		return appDef;
	}

	public VirtueTemplate updateVirtueTemplate(User user, String templateId, VirtueTemplate template) {
		if (!templateId.equals(template.getId())) {
			template = new VirtueTemplate(templateId, template);
		}
		template.setLastEditor(user.getUsername());
		template.setLastModification(new Date());
		templateManager.addVirtueTemplate(template);
		return template;
	}

	public VirtualMachineTemplate updateVmTemplate(User user, String templateId, VirtualMachineTemplate vmTemplate) {
		if (!templateId.equals(vmTemplate.getId())) {
			vmTemplate = new VirtualMachineTemplate(templateId, vmTemplate);
		}
		vmTemplate.setLastEditor(user.getUsername());
		vmTemplate.setLastModification(new Date());
		templateManager.addVmTemplate(vmTemplate);
		return vmTemplate;
	}

	public void deleteApplicationDefinition(User user, String templateId) {
		templateManager.deleteApplicationDefinition(templateId);
	}

	public void deleteVmTemplate(User user, String templateId) {
		templateManager.deleteVmTemplate(templateId);
	}

	public void deleteVirtue(User user, String templateId) {
		templateManager.deleteVirtueTemplate(templateId);
	}
}
