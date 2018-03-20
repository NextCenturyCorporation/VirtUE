package com.ncc.savior.virtueadmin.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
import com.ncc.savior.virtueadmin.security.SecurityUserService;
import com.ncc.savior.virtueadmin.util.SaviorException;
import com.ncc.savior.virtueadmin.virtue.IActiveVirtueManager;

/**
 * Service that provides admin function like creating Virtue templates, Vm
 * Templates, applications as well as user functions. All actions require
 * ROLE_ADMIN.
 */
public class AdminService {

	private IActiveVirtueManager virtueManager;
	private ITemplateManager templateManager;
	private IUserManager userManager;

	@Autowired
	private SessionRegistry sessionRegistry;

	@Autowired
	private SecurityUserService securityService;

	private String initialAdmin;

	public AdminService(IActiveVirtueManager virtueManager, ITemplateManager templateManager, IUserManager userManager,
			String initialAdmin) {
		super();
		this.virtueManager = virtueManager;
		this.templateManager = templateManager;
		this.userManager = userManager;
		this.initialAdmin = initialAdmin;
		addInitialUser();
	}

	private void addInitialUser() {
		Iterable<VirtueUser> users = userManager.getAllUsers();
		if (initialAdmin != null && !initialAdmin.trim().equals("") && !users.iterator().hasNext()) {
			String[] admins = initialAdmin.split(",");
			for (String admin : admins) {
				admin = admin.trim();
				if (admin.equals("")) {
					return;
				}
				Collection<String> authorities = new ArrayList<String>(2);
				authorities.add("ROLE_ADMIN");
				authorities.add("ROLE_USER");
				VirtueUser user = new VirtueUser(admin, authorities);
				userManager.addUser(user);
			}
		}
	}

	public AdminService(ITemplateManager templateManager) {
		verifyAndReturnUser();
		this.templateManager = templateManager;
	}

	public Iterable<VirtueTemplate> getAllVirtueTemplates() {
		verifyAndReturnUser();
		return templateManager.getAllVirtueTemplates();
	}

	public Iterable<VirtualMachineTemplate> getAllVmTemplates() {
		verifyAndReturnUser();
		return templateManager.getAllVirtualMachineTemplates();
	}

	public Iterable<ApplicationDefinition> getAllApplicationTemplates() {
		verifyAndReturnUser();
		return templateManager.getAllApplications();
	}

	public Iterable<VirtueInstance> getAllActiveVirtues() {
		verifyAndReturnUser();
		return virtueManager.getAllActiveVirtues();
	}

	public Iterable<VirtueTemplate> getVirtueTemplatesForUser(String username) {
		verifyAndReturnUser();
		VirtueUser user = userManager.getUser(username);
		if (user != null) {
			Map<String, VirtueTemplate> vts = templateManager.getVirtueTemplatesForUser(user);
			return vts.values();
		} else {
			throw new SaviorException(SaviorException.USER_NOT_FOUND, "User=" + username + " not found");
		}
	}

	public Iterable<VirtueInstance> getAllActiveVirtuesForUser(String username) {
		verifyAndReturnUser();
		VirtueUser user = userManager.getUser(username);
		if (user != null) {
			Collection<VirtueInstance> vs = virtueManager.getVirtuesForUser(user);
			return vs;
		} else {
			throw new SaviorException(SaviorException.USER_NOT_FOUND, "User=" + username + " not found");
		}
	}

	public VirtueTemplate getVirtueTemplate(String templateId) {
		verifyAndReturnUser();
		Optional<VirtueTemplate> opt = templateManager.getVirtueTemplate(templateId);
		return opt.isPresent() ? opt.get() : null;
	}

	public VirtualMachineTemplate getVmTemplate(String templateId) {
		verifyAndReturnUser();
		Optional<VirtualMachineTemplate> opt = templateManager.getVmTemplate(templateId);
		return opt.isPresent() ? opt.get() : null;
	}

	public VirtueInstance getActiveVirtue(String virtueId) {
		verifyAndReturnUser();
		return virtueManager.getActiveVirtue(virtueId);
	}

	public ApplicationDefinition getApplicationDefinition(String templateId) {
		verifyAndReturnUser();
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
		Collection<String> vmtIds = template.getVirtualMachineTemplateIds();
		Iterator<VirtualMachineTemplate> itr = templateManager.getVmTemplates(vmtIds).iterator();
		if (!templateId.equals(template.getId())) {
			template = new VirtueTemplate(templateId, template);
		}
		
		Set<VirtualMachineTemplate> vmTemplates=new HashSet<VirtualMachineTemplate>();
		while(itr.hasNext()) {
			vmTemplates.add(itr.next());
		}
		template.setVmTemplates(vmTemplates);
		template.setLastEditor(user.getUsername());
		template.setLastModification(new Date());
		templateManager.addVirtueTemplate(template);
		return template;
	}

	public VirtualMachineTemplate updateVmTemplate(String templateId, VirtualMachineTemplate vmTemplate) {
		VirtueUser user = verifyAndReturnUser();
		Collection<String> appIds = vmTemplate.getApplicationIds();
		Iterator<ApplicationDefinition> itr = templateManager.getApplications(appIds).iterator();
		if (!templateId.equals(vmTemplate.getId())) {
			vmTemplate = new VirtualMachineTemplate(templateId, vmTemplate);
		}
		
		Collection<ApplicationDefinition> applications=new HashSet<ApplicationDefinition>();
		while(itr.hasNext()) {
			applications.add(itr.next());
		}
		vmTemplate.setApplications(applications);
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
		Collection<String> vts = newUser.getVirtueTemplateIds();
		if (vts != null && !vts.isEmpty()) {
			Collection<VirtueTemplate> usersVirtueTemplates = new ArrayList<VirtueTemplate>();
			Iterable<VirtueTemplate> iterable = templateManager.getVirtueTemplates(vts);
			Iterator<VirtueTemplate> itr = iterable.iterator();
			while (itr.hasNext()) {
				usersVirtueTemplates.add(itr.next());
			}
		}
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

	public List<VirtueUser> getActiveUsers() {
		verifyAndReturnUser();
		List<Object> principals = sessionRegistry.getAllPrincipals();
		List<VirtueUser> users = new ArrayList<VirtueUser>(principals.size());
		for (Object p : principals) {
			User user = (User) p;
			ArrayList<String> auths = new ArrayList<String>();
			for (GrantedAuthority a : user.getAuthorities()) {
				auths.add(a.getAuthority());
			}
			VirtueUser u = userManager.getUser(user.getUsername());
			users.add(u);
		}
		return users;

	}

	public VirtueSession getActiveSession(String sessionId) {
		verifyAndReturnUser();
		if (sessionId != null) {
			SessionInformation session = sessionRegistry.getSessionInformation(sessionId);
			VirtueSession vs = VirtueSession.fromSessionInformation(session);
			return vs;
		} else {
			return null;
		}
	}

	public void invalidateSession(String sessionId) {
		verifyAndReturnUser();
		if (sessionId != null) {
			SessionInformation session = sessionRegistry.getSessionInformation(sessionId);
			session.expireNow();
		}
	}

	public void logoutUser(String username) {
		verifyAndReturnUser();
		List<Object> principals = sessionRegistry.getAllPrincipals();
		for (Object principal : principals) {
			User user = (User) principal;
			if (user.getUsername().equals(username)) {
				List<SessionInformation> sessions = sessionRegistry.getAllSessions(user, false);
				for (SessionInformation session : sessions) {
					session.expireNow();
				}
			}
		}
		throw new SaviorException(SaviorException.REQUESTED_USER_NOT_LOGGED_IN,
				"User=" + username + " was not logged in");

	}

	public Map<String, List<String>> getActiveSessions() {
		verifyAndReturnUser();
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

	private VirtueUser verifyAndReturnUser() {
		VirtueUser user = securityService.getCurrentUser();
		if (!user.getAuthorities().contains("ROLE_ADMIN")) {
			throw new SaviorException(SaviorException.UNKNOWN_ERROR, "User did not have ADMIN role");
		}
		return user;
	}
}
