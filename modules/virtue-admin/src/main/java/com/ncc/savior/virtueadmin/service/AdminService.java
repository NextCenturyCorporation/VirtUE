package com.ncc.savior.virtueadmin.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.User;

import com.ncc.savior.virtueadmin.data.ITemplateManager;
import com.ncc.savior.virtueadmin.data.IUserManager;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.VirtualMachineTemplate;
import com.ncc.savior.virtueadmin.model.VirtueInstance;
import com.ncc.savior.virtueadmin.model.VirtueSession;
import com.ncc.savior.virtueadmin.model.VirtueTemplate;
import com.ncc.savior.virtueadmin.model.VirtueUser;
import com.ncc.savior.virtueadmin.security.UserService;
import com.ncc.savior.virtueadmin.util.SaviorException;
import com.ncc.savior.virtueadmin.virtue.IActiveVirtueManager;

public class AdminService {

	private IActiveVirtueManager virtueManager;
	private ITemplateManager templateManager;
	private IUserManager userManager;

	@Autowired
	private SessionRegistry sessionRegistry;

	public AdminService(IActiveVirtueManager virtueManager, ITemplateManager templateManager,
			IUserManager userManager) {
		super();
		this.virtueManager = virtueManager;
		this.templateManager = templateManager;
		this.userManager = userManager;
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

	public VirtueTemplate getVirtueTemplate(String templateId) {
		Optional<VirtueTemplate> opt = templateManager.getVirtueTemplate(templateId);
		return opt.isPresent() ? opt.get() : null;
	}

	public VirtualMachineTemplate getVmTemplate(String templateId) {
		Optional<VirtualMachineTemplate> opt = templateManager.getVmTemplate(templateId);
		return opt.isPresent() ? opt.get() : null;
	}

	public VirtueInstance getActiveVirtue(String virtueId) {
		return virtueManager.getActiveVirtue(virtueId);
	}

	public ApplicationDefinition getApplicationDefinition(String templateId) {
		Optional<ApplicationDefinition> opt = templateManager.getApplicationDefinition(templateId);
		return opt.isPresent() ? opt.get() : null;
	}

	public VirtueTemplate createNewVirtueTemplate(VirtueTemplate template) {
		verifyAndReturnUser();
		String id = UUID.randomUUID().toString();
		return updateVirtueTemplate(id, template);
	}

	public ApplicationDefinition createNewApplicationDefinition(ApplicationDefinition appDef) {
		verifyAndReturnUser();
		String id = UUID.randomUUID().toString();
		return updateApplicationDefinitions(id, appDef);
	}

	public VirtualMachineTemplate createVmTemplate(VirtualMachineTemplate vmTemplate) {
		verifyAndReturnUser();
		String id = UUID.randomUUID().toString();
		return updateVmTemplate(id, vmTemplate);
	}

	public ApplicationDefinition updateApplicationDefinitions(String templateId, ApplicationDefinition appDef) {
		verifyAndReturnUser();
		if (!templateId.equals(appDef.getId())) {
			appDef = new ApplicationDefinition(templateId, appDef);
		}
		templateManager.addApplicationDefinition(appDef);
		return appDef;
	}

	public VirtueTemplate updateVirtueTemplate(String templateId, VirtueTemplate template) {
		VirtueUser user = verifyAndReturnUser();
		if (!templateId.equals(template.getId())) {
			template = new VirtueTemplate(templateId, template);
		}
		template.setLastEditor(user.getUsername());
		template.setLastModification(new Date());
		templateManager.addVirtueTemplate(template);
		return template;
	}

	public VirtualMachineTemplate updateVmTemplate(String templateId, VirtualMachineTemplate vmTemplate) {
		VirtueUser user = verifyAndReturnUser();
		if (!templateId.equals(vmTemplate.getId())) {
			vmTemplate = new VirtualMachineTemplate(templateId, vmTemplate);
		}
		vmTemplate.setLastEditor(user.getUsername());
		vmTemplate.setLastModification(new Date());
		templateManager.addVmTemplate(vmTemplate);
		return vmTemplate;
	}

	public void deleteApplicationDefinition(String templateId) {
		verifyAndReturnUser();
		templateManager.deleteApplicationDefinition(templateId);
	}

	public void deleteVmTemplate(String templateId) {
		verifyAndReturnUser();
		templateManager.deleteVmTemplate(templateId);
	}

	public void deleteVirtueTemplate(String templateId) {
		verifyAndReturnUser();
		templateManager.deleteVirtueTemplate(templateId);
	}

	public void deleteVirtue(String instanceId) {
		verifyAndReturnUser();
		virtueManager.adminDeleteVirtue(instanceId);
	}

	public VirtueUser createUpdateUser(VirtueUser newUser) {
		verifyAndReturnUser();
		userManager.addUser(newUser);
		return newUser;
	}

	public VirtueUser getUser(String usernameToRetrieve) {
		verifyAndReturnUser();
		return userManager.getUser(usernameToRetrieve);
	}

	public void removeUser(String usernameToRemove) {
		verifyAndReturnUser();
		userManager.removeUser(usernameToRemove);
	}

	public Iterable<VirtueUser> getAllUsers() {
		verifyAndReturnUser();
		return userManager.getAllUsers();
	}

	public void assignTemplateToUser(String templateId, String username) {
		verifyAndReturnUser();
		VirtueUser user = userManager.getUser(username);
		if (user != null) {
			templateManager.assignVirtueTemplateToUser(user, templateId);
		} else {
			throw new SaviorException(SaviorException.USER_NOT_FOUND, "User=" + username + " was not found");
		}
	}

	public void revokeTemplateFromUser(String templateId, String username) {
		verifyAndReturnUser();
		VirtueUser user = userManager.getUser(username);
		if (user != null) {
			templateManager.revokeVirtueTemplateFromUser(user, templateId);
		} else {
			throw new SaviorException(SaviorException.USER_NOT_FOUND, "User=" + username + " was not found");
		}
	}

	private VirtueUser verifyAndReturnUser() {
		VirtueUser user = UserService.getCurrentUser();
		if (!user.getAuthorities().contains("ROLE_ADMIN")) {
			throw new SaviorException(SaviorException.UNKNOWN_ERROR, "User did not have ADMIN role");
		}
		return user;
	}

	public List<VirtueUser> getActiveUsers() {
		List<Object> principals = sessionRegistry.getAllPrincipals();
		List<VirtueUser> users = new ArrayList<VirtueUser>(principals.size());
		for (Object p : principals) {
			User user = (User) p;
			ArrayList<String> auths = new ArrayList<String>();
			for (GrantedAuthority a : user.getAuthorities()) {
				auths.add(a.getAuthority());
			}
			VirtueUser u = new VirtueUser(user.getUsername(), auths);
			users.add(u);
		}
		return users;

	}

	public VirtueSession getActiveSession(String sessionId) {
		if (sessionId != null) {
			SessionInformation session = sessionRegistry.getSessionInformation(sessionId);
			VirtueSession vs = VirtueSession.fromSessionInformation(session);
			return vs;
		} else {
			return null;
		}
	}

	public void invalidateSession(String sessionId) {
		if (sessionId != null) {
			SessionInformation session = sessionRegistry.getSessionInformation(sessionId);
			session.expireNow();
		}
	}

	public Map<String, List<String>> getActiveSessions() {
		List<Object> principals = sessionRegistry.getAllPrincipals();
		Map<String, List<String>> sessionMap = new HashMap<String, List<String>>();
		for (Object principal : principals) {
			User user = (User) principal;
			List<SessionInformation> sessions = sessionRegistry.getAllSessions(principal, false);
			ArrayList<String> list = new ArrayList<String>();
			for (SessionInformation s : sessions) {
				list.add(s.getSessionId());
			}
			sessionMap.put(user.getUsername(), list);
		}
		return sessionMap;
	}
}
