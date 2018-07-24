package com.ncc.savior.virtueadmin.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.User;

import com.ncc.savior.util.JavaUtil;
import com.ncc.savior.util.SaviorException;
import com.ncc.savior.virtueadmin.data.ITemplateManager;
import com.ncc.savior.virtueadmin.data.IUserManager;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.IconModel;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VirtualMachineTemplate;
import com.ncc.savior.virtueadmin.model.VirtueInstance;
import com.ncc.savior.virtueadmin.model.VirtueSession;
import com.ncc.savior.virtueadmin.model.VirtueState;
import com.ncc.savior.virtueadmin.model.VirtueTemplate;
import com.ncc.savior.virtueadmin.model.VirtueUser;
import com.ncc.savior.virtueadmin.security.SecurityUserService;
import com.ncc.savior.virtueadmin.virtue.IActiveVirtueManager;

/**
 * Service that provides admin function like creating Virtue templates, Vm
 * Templates, applications as well as user functions. All actions require
 * ROLE_ADMIN.
 */
public class AdminService {
	public static final String DEFAULT_ICON_KEY = "DEFAULT";
	private IActiveVirtueManager virtueManager;
	private ITemplateManager templateManager;
	private IUserManager userManager;

	@Autowired
	private SessionRegistry sessionRegistry;

	@Autowired
	private SecurityUserService securityService;

	private String initialAdmin;

	@Value("${virtue.sensing.redirectUrl}")
	private String sensingUri;

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
				VirtueUser user = new VirtueUser(admin, authorities, true);
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
		Map<String, VirtueTemplate> vts = templateManager.getVirtueTemplatesForUser(user);
		return vts.values();
	}

	public Iterable<VirtueInstance> getAllActiveVirtuesForUser(String username) {
		verifyAndReturnUser();
		VirtueUser user = userManager.getUser(username);
		Collection<VirtueInstance> vs = virtueManager.getVirtuesForUser(user);
		return vs;
	}

	public VirtueTemplate getVirtueTemplate(String templateId) {
		verifyAndReturnUser();
		VirtueTemplate viTemplate = templateManager.getVirtueTemplate(templateId);
		return viTemplate;
	}

	public VirtueTemplate toggleVirtueTemplateEnabled(String templateId) {
		VirtueUser user = verifyAndReturnUser();
		VirtueTemplate viTemplate = templateManager.getVirtueTemplate(templateId);
		boolean enabled = viTemplate.isEnabled();
		viTemplate.setEnabled(!enabled);
		viTemplate.setLastModification(new Date());
		viTemplate.setLastEditor(user.getUsername());
		templateManager.addVirtueTemplate(viTemplate);
		return viTemplate;
	}

	public VirtualMachineTemplate toggleVirtualMachineTemplateEnabled(String templateId) {
		VirtueUser user = verifyAndReturnUser();
		VirtualMachineTemplate vmtTemplate = templateManager.getVmTemplate(templateId);
		boolean enabled = vmtTemplate.isEnabled();
		vmtTemplate.setEnabled(!enabled);
		vmtTemplate.setLastModification(new Date());
		vmtTemplate.setLastEditor(user.getUsername());
		templateManager.addVmTemplate(vmtTemplate);
		return vmtTemplate;
	}

	public VirtualMachineTemplate getVmTemplate(String templateId) {
		verifyAndReturnUser();
		VirtualMachineTemplate vmTemplate = templateManager.getVmTemplate(templateId);
		return vmTemplate;
	}

	public VirtueInstance getActiveVirtue(String virtueId) {
		verifyAndReturnUser();
		return virtueManager.getActiveVirtue(virtueId);
	}

	public ApplicationDefinition getApplicationDefinition(String templateId) {
		verifyAndReturnUser();
		ApplicationDefinition app = templateManager.getApplicationDefinition(templateId);
		return app;
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
		if (appDef.getId() == null) {
			appDef.setId(templateId);
		}
		if (!templateId.equals(appDef.getId())) {
			appDef = new ApplicationDefinition(templateId, appDef);
		}
		templateManager.addApplicationDefinition(appDef);
		return appDef;
	}

	public VirtueTemplate updateVirtueTemplate(String templateId, VirtueTemplate template) {
		VirtueUser user = verifyAndReturnUser();
		Collection<String> vmtIds = template.getVirtualMachineTemplateIds();
		Iterable<VirtualMachineTemplate> vmts;
		if (vmtIds == null) {
			vmts = new ArrayList<VirtualMachineTemplate>();
		} else {
			vmts = templateManager.getVmTemplates(vmtIds);
		}
		Iterator<VirtualMachineTemplate> itr = vmts.iterator();
		if (!templateId.equals(template.getId())) {
			template = new VirtueTemplate(templateId, template);
			template.setUserCreatedBy(user.getUsername());
			template.setTimeCreatedAt(new Date());
		}

		Set<VirtualMachineTemplate> vmTemplates = new HashSet<VirtualMachineTemplate>();
		while (itr.hasNext()) {
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
		if (appIds == null) {
			appIds = new ArrayList<String>();
		}
		Iterator<ApplicationDefinition> itr = templateManager.getApplications(appIds).iterator();
		if (vmTemplate.getId() == null) {
			vmTemplate.setId(templateId);
		}
		if (!templateId.equals(vmTemplate.getId())) {
			vmTemplate = new VirtualMachineTemplate(templateId, vmTemplate);
			vmTemplate.setUserCreatedBy(user.getUsername());
			vmTemplate.setTimeCreatedAt(new Date());
		}

		Collection<ApplicationDefinition> applications = new HashSet<ApplicationDefinition>();
		while (itr.hasNext()) {
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
			newUser.setVirtueTemplates(usersVirtueTemplates);
		}
		userManager.addUser(newUser);
		return newUser;
	}

	public VirtueUser getUser(String usernameToRetrieve) {
		verifyAndReturnUser();
		VirtueUser user = userManager.getUser(usernameToRetrieve);
		return user;
	}

	public void removeUser(String usernameToRemove) {
		verifyAndReturnUser();
		try {
			VirtueUser user = userManager.getUser(usernameToRemove);
			if (user != null) {
				Collection<VirtueInstance> virtues = virtueManager.getVirtuesForUser(user);
				virtues.parallelStream().forEach((v) -> {
					virtueManager.deleteVirtue(user, v.getId());
				});
			}
		} finally {
			userManager.removeUser(usernameToRemove);
		}
	}

	public void enableDisableUser(String username, Boolean enable) {
		verifyAndReturnUser();
		userManager.enableDisableUser(username, enable);
		if (!enable) {
			// disabling a user has the following side effects:
			// stop their running virtues
			VirtueUser user = userManager.getUser(username);
			Collection<VirtueInstance> virtues = virtueManager.getVirtuesForUser(user);
			virtues.parallelStream().filter((v) -> {
				return v.getState().equals(VirtueState.RUNNING) || v.getState().equals(VirtueState.CREATING)
						|| v.getState().equals(VirtueState.LAUNCHING) || v.getState().equals(VirtueState.RESUMING);

			}).forEach((v) -> {
				virtueManager.stopVirtue(user, v.getId());
			});
		}
	}

	public Iterable<VirtueUser> getAllUsers() {
		verifyAndReturnUser();
		return userManager.getAllUsers();
	}

	public void assignTemplateToUser(String templateId, String username) {
		verifyAndReturnUser();
		VirtueUser user = userManager.getUser(username);
		templateManager.assignVirtueTemplateToUser(user, templateId);
	}

	public void revokeTemplateFromUser(String templateId, String username) {
		verifyAndReturnUser();
		VirtueUser user = userManager.getUser(username);
		templateManager.revokeVirtueTemplateFromUser(user, templateId);
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
			if (principal == null) {
				continue;
			}
			String username = (principal instanceof User ? ((User) principal).getUsername() : principal.toString());
			List<SessionInformation> sessions = sessionRegistry.getAllSessions(principal, false);
			ArrayList<String> list = new ArrayList<String>();
			for (SessionInformation s : sessions) {
				list.add(s.getSessionId());
			}
			sessionMap.put(username, list);
		}
		return sessionMap;
	}

	public void uploadIcon(String iconKey, InputStream inputStream) throws IOException {
		verifyAndReturnUser();
		byte[] bytes = IOUtils.toByteArray(inputStream);
		templateManager.addIcon(iconKey, bytes);
	}

	public IconModel getIcon(String iconKey) {
		verifyAndReturnUser();
		IconModel icon = templateManager.getIcon(iconKey);
		if (icon == null) {
			icon = templateManager.getIcon(DEFAULT_ICON_KEY);
		}
		return icon;
	}

	public void deleteIcon(String iconKey) {
		verifyAndReturnUser();
		if (!DEFAULT_ICON_KEY.equals(iconKey)) {
			templateManager.removeIcon(iconKey);
		}
	}

	public Set<String> getAllIconKeys() {
		verifyAndReturnUser();
		return templateManager.getAllIconKeys();
	}

	private VirtueUser verifyAndReturnUser() {
		VirtueUser user = securityService.getCurrentUser();
		if (!user.getAuthorities().contains("ROLE_ADMIN")) {
			throw new SaviorException(SaviorException.UNKNOWN_ERROR, "User did not have ADMIN role");
		}
		return user;
	}

	public String getSensingReponse() throws IOException {
		if (JavaUtil.isNotEmpty(sensingUri)) {
			Client client = ClientBuilder.newClient();
			Response response = client.target(sensingUri).request(MediaType.APPLICATION_JSON_TYPE).get();
			InputStream in = (InputStream) response.getEntity();
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			String line;
			StringBuilder sb = new StringBuilder();
			while ((line = reader.readLine()) != null) {
				sb.append(line).append("\n");
			}
			return sb.toString();
		} else {
			throw new IllegalArgumentException("No sensing URI was set");
		}
	}

	public Iterable<VirtualMachine> getAllVirtualMachines() {
		verifyAndReturnUser();
		return virtueManager.getAllVirtualMachines();
	}

	public VirtualMachine getVm(String id) {
		verifyAndReturnUser();
		return virtueManager.getVm(id);
	}
	
	public void rebootVm(String vmId, String virtueId) {
		verifyAndReturnUser();
		virtueManager.rebootVm(vmId, virtueId);
	}
}
