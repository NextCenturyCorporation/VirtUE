package com.ncc.savior.virtueadmin.service;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import com.ncc.savior.virtueadmin.data.ITemplateManager;
import com.ncc.savior.virtueadmin.data.IUserManager;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.VirtueUser;
import com.ncc.savior.virtueadmin.model.VirtualMachineTemplate;
import com.ncc.savior.virtueadmin.model.VirtueInstance;
import com.ncc.savior.virtueadmin.model.VirtueTemplate;
import com.ncc.savior.virtueadmin.virtue.IActiveVirtueManager;

public class AdminService {

	private IActiveVirtueManager virtueManager;
	private ITemplateManager templateManager;
	private IUserManager userManager;

	public AdminService(IActiveVirtueManager virtueManager, ITemplateManager templateManager, IUserManager userManager) {
		super();
		this.virtueManager = virtueManager;
		this.templateManager = templateManager;
		this.userManager = userManager;
	}

	public AdminService(ITemplateManager templateManager) {
		this.templateManager = templateManager;
	}

	public Iterable<VirtueTemplate> getAllVirtueTemplates(VirtueUser user) {
		return templateManager.getAllVirtueTemplates();
	}

	public Iterable<VirtualMachineTemplate> getAllVmTemplates(VirtueUser user) {
		return templateManager.getAllVirtualMachineTemplates();
	}

	public Iterable<ApplicationDefinition> getAllApplicationTemplates(VirtueUser user) {
		return templateManager.getAllApplications();
	}

	public Iterable<VirtueInstance> getAllActiveVirtues(VirtueUser user) {
		return virtueManager.getAllActiveVirtues();
	}

	public VirtueTemplate getVirtueTemplate(VirtueUser user, String templateId) {
		Optional<VirtueTemplate> opt = templateManager.getVirtueTemplate(templateId);
		return opt.isPresent() ? opt.get() : null;
	}

	public VirtualMachineTemplate getVmTemplate(VirtueUser user, String templateId) {
		Optional<VirtualMachineTemplate> opt = templateManager.getVmTemplate(templateId);
		return opt.isPresent() ? opt.get() : null;
	}

	public VirtueInstance getActiveVirtue(VirtueUser user, String virtueId) {
		return virtueManager.getActiveVirtue(virtueId);
	}

	public ApplicationDefinition getApplicationDefinition(VirtueUser user, String templateId) {
		Optional<ApplicationDefinition> opt = templateManager.getApplicationDefinition(templateId);
		return opt.isPresent() ? opt.get() : null;
	}

	public VirtueTemplate createNewVirtueTemplate(VirtueUser user, VirtueTemplate template) {
		String id = UUID.randomUUID().toString();
		return updateVirtueTemplate(user, id, template);
	}

	public ApplicationDefinition createNewApplicationDefinition(VirtueUser user, ApplicationDefinition appDef) {
		String id = UUID.randomUUID().toString();
		return updateApplicationDefinitions(user, id, appDef);
	}

	public VirtualMachineTemplate createVmTemplate(VirtueUser user, VirtualMachineTemplate vmTemplate) {
		String id = UUID.randomUUID().toString();
		return updateVmTemplate(user, id, vmTemplate);
	}

	public ApplicationDefinition updateApplicationDefinitions(VirtueUser user, String templateId,
			ApplicationDefinition appDef) {
		if (!templateId.equals(appDef.getId())) {
			appDef = new ApplicationDefinition(templateId, appDef);
		}
		templateManager.addApplicationDefinition(appDef);
		return appDef;
	}

	public VirtueTemplate updateVirtueTemplate(VirtueUser user, String templateId, VirtueTemplate template) {
		if (!templateId.equals(template.getId())) {
			template = new VirtueTemplate(templateId, template);
		}
		template.setLastEditor(user.getUsername());
		template.setLastModification(new Date());
		templateManager.addVirtueTemplate(template);
		return template;
	}

	public VirtualMachineTemplate updateVmTemplate(VirtueUser user, String templateId, VirtualMachineTemplate vmTemplate) {
		if (!templateId.equals(vmTemplate.getId())) {
			vmTemplate = new VirtualMachineTemplate(templateId, vmTemplate);
		}
		vmTemplate.setLastEditor(user.getUsername());
		vmTemplate.setLastModification(new Date());
		templateManager.addVmTemplate(vmTemplate);
		return vmTemplate;
	}

	public void deleteApplicationDefinition(VirtueUser user, String templateId) {
		templateManager.deleteApplicationDefinition(templateId);
	}

	public void deleteVmTemplate(VirtueUser user, String templateId) {
		templateManager.deleteVmTemplate(templateId);
	}

	public void deleteVirtue(VirtueUser user, String templateId) {
		templateManager.deleteVirtueTemplate(templateId);
	}

	public VirtueUser createUpdateUser(VirtueUser user, VirtueUser newUser) {
		userManager.addUser(newUser);
		return newUser;
	}

	public VirtueUser getUser(VirtueUser user, String usernameToRetrieve) {
		return userManager.getUser(usernameToRetrieve);
	}

	public void removeUser(VirtueUser user, String usernameToRemove) {
		userManager.removeUser(user);
	}

	public Iterable<VirtueUser> getAllUsers(VirtueUser user) {
		return userManager.getAllUsers();
	}
}
