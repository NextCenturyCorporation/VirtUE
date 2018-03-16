package com.ncc.savior.virtueadmin.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.User;

import com.ncc.savior.util.ConversionUtil;
import com.ncc.savior.virtueadmin.data.ITemplateManager;
import com.ncc.savior.virtueadmin.data.IUserManager;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.VirtueSession;
import com.ncc.savior.virtueadmin.security.SecurityUserService;
import com.ncc.savior.virtueadmin.util.SaviorException;
import com.ncc.savior.virtueadmin.virtue.IActiveVirtueManager;

import dto.VirtualMachineTemplateDto;
import dto.VirtueInstanceDto;
import dto.VirtueTemplateDto;
import dto.VirtueUserDto;
import persistance.JpaVirtualMachineTemplate;
import persistance.JpaVirtueInstance;
import persistance.JpaVirtueTemplate;
import persistance.JpaVirtueUser;

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
		Iterable<JpaVirtueUser> users = userManager.getAllUsers();
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
				JpaVirtueUser user = new JpaVirtueUser(admin, authorities);
				userManager.addUser(user);
			}
		}
	}

	public AdminService(ITemplateManager templateManager) {
		verifyAndReturnUser();
		this.templateManager = templateManager;
	}

	public Iterable<VirtueTemplateDto> getAllVirtueTemplates() {
		verifyAndReturnUser();
		Iterable<JpaVirtueTemplate> itr = templateManager.getAllVirtueTemplates();
		return ConversionUtil.virtueTemplateIterable(itr);
	}

	public Iterable<VirtualMachineTemplateDto> getAllVmTemplates() {
		verifyAndReturnUser();
		Iterable<JpaVirtualMachineTemplate> itr = templateManager.getAllVirtualMachineTemplates();
		return ConversionUtil.vmTemplateIterable(itr);
	}

	public Iterable<ApplicationDefinition> getAllApplicationTemplates() {
		verifyAndReturnUser();
		return templateManager.getAllApplications();
	}

	public Iterable<VirtueInstanceDto> getAllActiveVirtues() {
		verifyAndReturnUser();
		Iterable<JpaVirtueInstance> itr = virtueManager.getAllActiveVirtues();
		return ConversionUtil.virtueInstanceIterable(itr);
	}

	public Iterable<VirtueTemplateDto> getVirtueTemplatesForUser(String username) {
		verifyAndReturnUser();
		JpaVirtueUser user = userManager.getUser(username);
		if (user != null) {
			Map<String, JpaVirtueTemplate> vts = templateManager.getVirtueTemplatesForUser(user);
			return ConversionUtil.virtueTemplateIterable(vts.values());
		} else {
			throw new SaviorException(SaviorException.USER_NOT_FOUND, "User=" + username + " not found");
		}
	}

	public Iterable<VirtueInstanceDto> getAllActiveVirtuesForUser(String username) {
		verifyAndReturnUser();
		JpaVirtueUser user = userManager.getUser(username);
		if (user != null) {
			Collection<JpaVirtueInstance> jpaVms = virtueManager.getVirtuesForUser(user);
			return ConversionUtil.virtueInstanceIterable(jpaVms);
		} else {
			throw new SaviorException(SaviorException.USER_NOT_FOUND, "User=" + username + " not found");
		}
	}

	public VirtueTemplateDto getVirtueTemplate(String templateId) {
		verifyAndReturnUser();
		Optional<JpaVirtueTemplate> opt = templateManager.getVirtueTemplate(templateId);
		return opt.isPresent() ? new VirtueTemplateDto(opt.get()) : null;
	}

	public VirtualMachineTemplateDto getVmTemplate(String templateId) {
		verifyAndReturnUser();
		Optional<JpaVirtualMachineTemplate> opt = templateManager.getVmTemplate(templateId);
		return opt.isPresent() ? new VirtualMachineTemplateDto(opt.get()) : null;
	}

	public VirtueInstanceDto getActiveVirtue(String virtueId) {
		verifyAndReturnUser();
		JpaVirtueInstance persistVirtue = virtueManager.getActiveVirtue(virtueId);
		return new VirtueInstanceDto(persistVirtue, persistVirtue.getUsername(),
				ConversionUtil.hasIdIterable(persistVirtue.getVms()));
	}

	public ApplicationDefinition getApplicationDefinition(String templateId) {
		verifyAndReturnUser();
		Optional<ApplicationDefinition> opt = templateManager.getApplicationDefinition(templateId);
		return opt.isPresent() ? opt.get() : null;
	}

	public VirtueTemplateDto createNewVirtueTemplate(VirtueTemplateDto template) {
		verifyAndReturnUser();
		String id = UUID.randomUUID().toString();
		return updateVirtueTemplate(id, template);
	}

	public ApplicationDefinition createNewApplicationDefinition(ApplicationDefinition appDef) {
		verifyAndReturnUser();
		String id = UUID.randomUUID().toString();
		return updateApplicationDefinitions(id, appDef);
	}

	public VirtualMachineTemplateDto createVmTemplate(VirtualMachineTemplateDto vmTemplate) {
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

	public VirtueTemplateDto updateVirtueTemplate(String templateId, VirtueTemplateDto template) {
		verifyAndReturnUser();
		JpaVirtueUser user = verifyAndReturnUser();
		if (!templateId.equals(template.getId())) {
			template = new VirtueTemplateDto(templateId, template, template.getVmTemplateIds());
		}
		template.setLastEditor(user.getUsername());
		template.setLastModification(new Date());

		Iterable<JpaVirtualMachineTemplate> jvmt = templateManager.getVmTemplatesById(template.getVmTemplateIds());
		Collection<JpaVirtualMachineTemplate> vmTemplates = new ArrayList<JpaVirtualMachineTemplate>();
		Iterator<JpaVirtualMachineTemplate> itr = jvmt.iterator();
		while (itr.hasNext()) {
			vmTemplates.add(itr.next());
		}
		JpaVirtueTemplate t = new JpaVirtueTemplate(templateId, template.getName(), template.getVersion(), vmTemplates,
				template.getAwsTemplateName(), template.isEnabled(), template.getLastModification(),
				template.getLastEditor());
		templateManager.addVirtueTemplate(t);
		return template;
	}

	public VirtualMachineTemplateDto updateVmTemplate(String templateId, VirtualMachineTemplateDto vmTemplate) {
		verifyAndReturnUser();
		JpaVirtueUser user = verifyAndReturnUser();
		if (!templateId.equals(vmTemplate.getId())) {
			vmTemplate = new VirtualMachineTemplateDto(templateId, vmTemplate, vmTemplate.getApplicationIds());
		}
		vmTemplate.setLastEditor(user.getUsername());
		vmTemplate.setLastModification(new Date());

		Iterable<ApplicationDefinition> japps = templateManager
				.getApplicationDefinitions(vmTemplate.getApplicationIds());
		JpaVirtualMachineTemplate jvmt = new JpaVirtualMachineTemplate(vmTemplate, japps);

		templateManager.addVmTemplate(jvmt);
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

	public VirtueUserDto createUpdateUser(VirtueUserDto newUser) {
		verifyAndReturnUser();
		JpaVirtueUser juser = new JpaVirtueUser(newUser.getUsername(), newUser.getAuthorities());
		userManager.addUser(juser);
		return newUser;
	}

	public VirtueUserDto getUser(String usernameToRetrieve) {
		verifyAndReturnUser();
		JpaVirtueUser juser = userManager.getUser(usernameToRetrieve);
		return new VirtueUserDto(juser, ConversionUtil.hasIdIterable(juser.getVirtueTemplates()));
	}

	public void removeUser(String usernameToRemove) {
		verifyAndReturnUser();
		userManager.removeUser(usernameToRemove);
	}

	public Iterable<VirtueUserDto> getAllUsers() {
		verifyAndReturnUser();
		return ConversionUtil.userIterable(userManager.getAllUsers());
	}

	public void assignTemplateToUser(String templateId, String username) {
		verifyAndReturnUser();
		JpaVirtueUser user = userManager.getUser(username);
		if (user != null) {
			templateManager.assignVirtueTemplateToUser(user, templateId);
		} else {
			throw new SaviorException(SaviorException.USER_NOT_FOUND, "User=" + username + " was not found");
		}
	}

	public void revokeTemplateFromUser(String templateId, String username) {
		verifyAndReturnUser();
		JpaVirtueUser user = userManager.getUser(username);
		if (user != null) {
			templateManager.revokeVirtueTemplateFromUser(user, templateId);
		} else {
			throw new SaviorException(SaviorException.USER_NOT_FOUND, "User=" + username + " was not found");
		}
	}

	public List<VirtueUserDto> getActiveUsers() {
		verifyAndReturnUser();
		List<Object> principals = sessionRegistry.getAllPrincipals();
		List<VirtueUserDto> users = new ArrayList<VirtueUserDto>(principals.size());
		for (Object p : principals) {
			User user = (User) p;
			ArrayList<String> auths = new ArrayList<String>();
			for (GrantedAuthority a : user.getAuthorities()) {
				auths.add(a.getAuthority());
			}
			JpaVirtueUser u = userManager.getUser(user.getUsername());
			VirtueUserDto restUser = new VirtueUserDto(u,
					ConversionUtil.virtueTemplateCollection(u.getVirtueTemplates()));
			users.add(restUser);
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

	private JpaVirtueUser verifyAndReturnUser() {
		JpaVirtueUser user = securityService.getCurrentUser();
		if (!user.getAuthorities().contains("ROLE_ADMIN")) {
			throw new SaviorException(SaviorException.UNKNOWN_ERROR, "User did not have ADMIN role");
		}
		return user;
	}
}
