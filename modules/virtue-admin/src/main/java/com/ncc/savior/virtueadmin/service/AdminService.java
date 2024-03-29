/*
 * Copyright (C) 2019 Next Century Corporation
 * 
 * This file may be redistributed and/or modified under either the GPL
 * 2.0 or 3-Clause BSD license. In addition, the U.S. Government is
 * granted government purpose rights. For details, see the COPYRIGHT.TXT
 * file at the root of this project.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 * 
 * SPDX-License-Identifier: (GPL-2.0-only OR BSD-3-Clause)
 */
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.User;

import com.ncc.savior.util.JavaUtil;
import com.ncc.savior.util.SaviorErrorCode;
import com.ncc.savior.util.SaviorException;
import com.ncc.savior.virtueadmin.data.IResourceManager;
import com.ncc.savior.virtueadmin.data.ITemplateManager;
import com.ncc.savior.virtueadmin.data.IUserManager;
import com.ncc.savior.virtueadmin.infrastructure.aws.securitygroups.ISecurityGroupManager;
import com.ncc.savior.virtueadmin.infrastructure.aws.subnet.IVpcSubnetProvider;
import com.ncc.savior.virtueadmin.infrastructure.mixed.IXenVmProvider;
import com.ncc.savior.virtueadmin.infrastructure.persistent.PersistentStorageManager;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.FileSystem;
import com.ncc.savior.virtueadmin.model.IconModel;
import com.ncc.savior.virtueadmin.model.Printer;
import com.ncc.savior.virtueadmin.model.SecurityGroupPermission;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VirtualMachineTemplate;
import com.ncc.savior.virtueadmin.model.VirtueInstance;
import com.ncc.savior.virtueadmin.model.VirtuePersistentStorage;
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
	private static final Logger logger = LoggerFactory.getLogger(AdminService.class);
	public static final String DEFAULT_ICON_KEY = "DEFAULT";
	private IActiveVirtueManager virtueManager;
	private ITemplateManager templateManager;
	private IResourceManager resourceManager;
	private IUserManager userManager;
	private IXenVmProvider xenVmProvider;

	@Autowired
	private SessionRegistry sessionRegistry;

	@Autowired
	private SecurityUserService securityService;

	private PersistentStorageManager persistentStorageManager;

	private String initialAdmin;

	@Value("${virtue.sensing.redirectUrl}")
	private String sensingUri;

	@Value("${virtue.test:false}")
	private boolean test;

	private ISecurityGroupManager securityGroupManager;
	private IVpcSubnetProvider subnetProvider;

	public AdminService(IActiveVirtueManager virtueManager, ITemplateManager templateManager, IUserManager userManager,
			PersistentStorageManager persistentStorageManager, ISecurityGroupManager securityGroupManager,
			IResourceManager resourceManager, IVpcSubnetProvider subnetProvider, IXenVmProvider xenVmProvider,
			String initialAdmin) {
		super();
		this.virtueManager = virtueManager;
		this.templateManager = templateManager;
		this.userManager = userManager;
		this.resourceManager = resourceManager;
		this.persistentStorageManager = persistentStorageManager;
		this.initialAdmin = initialAdmin;
		this.securityGroupManager = securityGroupManager;
		this.subnetProvider = subnetProvider;
		this.xenVmProvider = xenVmProvider;
		addInitialUser();
	}

	public void sync() {
		Runnable syncRunnable = () -> {
			if (!test) {
				try {
					Collection<String> existingVirtueIds = new HashSet<String>();
					Iterable<VirtueInstance> virtueIds = virtueManager.getAllActiveVirtues();
					for (VirtueInstance virtueId : virtueIds) {
						existingVirtueIds.add(virtueId.getId());
					}
					// find pooled xen instances and their subnets
					Iterable<VirtualMachine> vms = virtueManager.getAllVirtualMachines();
					for (VirtualMachine vm : vms) {
						if (vm.getName().startsWith(IXenVmProvider.VM_NAME_POOL_PREFIX)) {
							existingVirtueIds.add(vm.getId());
						}
					}
					subnetProvider.sync(existingVirtueIds);

					Set<String> allTemplateIds = new HashSet<String>();
					Iterable<VirtueTemplate> templates = templateManager.getAllVirtueTemplates();
					for (VirtueTemplate template : templates) {
						allTemplateIds.add(template.getId());
					}
					securityGroupManager.sync(allTemplateIds);

					virtueManager.sync();
				} catch (Exception e) {
					logger.debug("Syncing failed", e);
				}
			}
		};
		Thread t = new Thread(syncRunnable, "Startup-Sync");
		t.setDaemon(true);
		t.start();
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
				authorities.add(VirtueUser.ROLE_ADMIN);
				authorities.add(VirtueUser.ROLE_USER);
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
		// return templateManager.getAllVirtueTemplates();
		Iterable<VirtueTemplate> virtuesItr = templateManager.getAllVirtueTemplates();
		ArrayList<VirtueTemplate> virtues = new ArrayList<VirtueTemplate>();
		virtuesItr.forEach(virtues::add);

		return virtues;
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

	public VirtueTemplate setVirtueTemplateStatus(String templateId, boolean newStatus) {
		VirtueUser user = verifyAndReturnUser();
		VirtueTemplate viTemplate = templateManager.getVirtueTemplate(templateId);
		viTemplate.setEnabled(newStatus);
		viTemplate.setLastModification(new Date());
		viTemplate.setLastEditor(user.getUsername());
		return templateManager.addVirtueTemplate(viTemplate);
	}

	public VirtualMachineTemplate setVirtualMachineTemplateStatus(String templateId, boolean newStatus) {
		VirtueUser user = verifyAndReturnUser();
		VirtualMachineTemplate vmtTemplate = templateManager.getVmTemplate(templateId);
		vmtTemplate.setEnabled(newStatus);
		vmtTemplate.setLastModification(new Date());
		vmtTemplate.setLastEditor(user.getUsername());
		templateManager.addVmTemplate(vmtTemplate);
		return vmtTemplate;
	}

	public Printer setPrinterStatus(String printerId, boolean newStatus) {
		verifyAndReturnUser();
		Printer printer = resourceManager.getPrinter(printerId);
		printer.setEnabled(newStatus);
		resourceManager.updatePrinter(printer.getId(), printer);
		return printer;
	}

	public FileSystem setFileSystemStatus(String fileSystemId, boolean newStatus) {
		verifyAndReturnUser();
		FileSystem fileSystem = resourceManager.getFileSystem(fileSystemId);
		fileSystem.setEnabled(newStatus);
		resourceManager.updateFileSystem(fileSystem.getId(), fileSystem);
		return fileSystem;
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

	public Printer getPrinter(String printerId) {
		verifyAndReturnUser();
		return resourceManager.getPrinter(printerId);
	}

	public FileSystem getFileSystem(String fileSystemId) {
		verifyAndReturnUser();
		return resourceManager.getFileSystem(fileSystemId);
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

	public Printer createPrinter(Printer printer) {
		verifyAndReturnUser();
		printer.setId(UUID.randomUUID().toString());
		return resourceManager.addPrinter(printer);
	}

	// just try deleting something? Either pick out those, or just redo the repo.
	// It's not that back. And then start saving it.

	public FileSystem createFileSystem(FileSystem fileSystem) {
		verifyAndReturnUser();
		fileSystem.setId(UUID.randomUUID().toString());
		return resourceManager.addFileSystem(fileSystem);
	}

	public Printer updatePrinter(String printerId, Printer printer) {
		verifyAndReturnUser();
		return resourceManager.updatePrinter(printerId, printer);
	}

	public FileSystem updateFileSystem(String fileSystemId, FileSystem fileSystem) {
		verifyAndReturnUser();
		return resourceManager.updateFileSystem(fileSystemId, fileSystem);
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

		if (!templateId.equals(template.getId())) {
			template = new VirtueTemplate(templateId, template);
			template.setUserCreatedBy(user.getUsername());
			template.setTimeCreatedAt(new Date());
		}

		// below code just converts IDs from json to actual objects for database.
		Collection<String> vmtIds = template.getVmTemplateIds();
		Iterable<VirtualMachineTemplate> vmts;
		if (vmtIds == null) {
			vmts = new ArrayList<VirtualMachineTemplate>();
		} else {
			vmts = templateManager.getVmTemplates(vmtIds);
		}
		Iterator<VirtualMachineTemplate> itr = vmts.iterator();
		Set<VirtualMachineTemplate> vmTemplateSet = new HashSet<VirtualMachineTemplate>();
		while (itr.hasNext()) {
			vmTemplateSet.add(itr.next());
		}

		// create list of printers from the virtueTemplate's printer id list, ignoring
		// duplicates.
		List<Printer> printerSet = new ArrayList<Printer>();
		Collection<String> printerIds = template.getPrinterIds();
		if (printerIds != null) {
			Iterable<Printer> itrPrinters = resourceManager.getPrinters(new HashSet<String>(printerIds));
			itrPrinters.forEach(printerSet::add); // go through the iterator and add each item to the printers
													// ArrayList.
		}

		List<FileSystem> fileSystems = new ArrayList<FileSystem>();
		Collection<String> fsIds = template.getFileSystemIds();
		if (fsIds != null) {
			Iterable<FileSystem> fsItr = resourceManager.getFileSystems(new HashSet<String>(fsIds));
			fsItr.forEach(fileSystems::add); // go through the iterator and add each item to the printers ArrayList.
		}

		template.setVmTemplates(vmTemplateSet);
		template.setPrinters(printerSet);
		template.setFileSystems(fileSystems);
		template.setLastEditor(user.getUsername());
		template.setLastModification(new Date());

		VirtueTemplate savedTemplate = templateManager.addVirtueTemplate(template);

		return savedTemplate;
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

	public void stopVirtue(String virtueId) {
		VirtueUser user = verifyAndReturnUser();
		VirtueInstance v = virtueManager.getActiveVirtue(virtueId);
		if (v.getState().equals(VirtueState.RUNNING) || v.getState().equals(VirtueState.CREATING)
				|| v.getState().equals(VirtueState.LAUNCHING) || v.getState().equals(VirtueState.RESUMING)) {
			virtueManager.stopVirtue(user, v.getId());
		}
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

	public void deletePrinter(String instanceId) {
		verifyAndReturnUser();
		resourceManager.deletePrinter(instanceId);
	}

	public void deleteFileSystem(String instanceId) {
		verifyAndReturnUser();
		resourceManager.deleteFileSystem(instanceId);
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
		throw new SaviorException(SaviorErrorCode.REQUESTED_USER_NOT_LOGGED_IN,
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

	public void uploadIcon(String iconKey, InputStream inputStream) {
		verifyAndReturnUser();
		try {
			byte[] bytes = IOUtils.toByteArray(inputStream);
			templateManager.addIcon(iconKey, bytes);
		} catch (IOException e) {
			throw new SaviorException(SaviorErrorCode.INVALID_INPUT, "Unable to read input stream into byte array", e);
		}
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
		if (!user.getAuthorities().contains(VirtueUser.ROLE_ADMIN)) {
			throw new SaviorException(SaviorErrorCode.USER_NOT_AUTHORIZED, "User did not have ADMIN role");
		}
		return user;
	}

	public String getSensingResponse() throws IOException {
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

	public void rebootVm(String vmId) {
		verifyAndReturnUser();
		virtueManager.rebootVm(vmId);
	}

	public Iterable<VirtuePersistentStorage> getAllPersistentStorage() {
		verifyAndReturnUser();
		return persistentStorageManager.getAllPersistentStorage();
	}

	public Iterable<VirtuePersistentStorage> getPersistentStorageForUser(String username) {
		verifyAndReturnUser();
		return persistentStorageManager.getPersistentStorageForUser(username);
	}

	public VirtuePersistentStorage getPersistentStorage(String username, String virtueTemplateId) {
		verifyAndReturnUser();
		VirtuePersistentStorage ps = persistentStorageManager.getPersistentStorage(username, virtueTemplateId);
		if (ps == null) {
			throw new SaviorException(SaviorErrorCode.STORAGE_NOT_FOUND,
					"Unable to find persistent storage for username=" + username + " virtueTemplateId="
							+ virtueTemplateId);
		}
		return ps;
	}

	public void deletePersistentStorage(String username, String virtueTemplateId) {
		verifyAndReturnUser();
		persistentStorageManager.deletePersistentStorage(username, virtueTemplateId);
	}

	public Collection<SecurityGroupPermission> getSecurityGroupPermissions(String groupId) {
		verifyAndReturnUser();
		return securityGroupManager.getSecurityGroupPermissionsByGroupId(groupId);
	}

	public Collection<SecurityGroupPermission> getSecurityGroupPermissionsByTemplate(String templateId) {
		verifyAndReturnUser();
		return securityGroupManager.getSecurityGroupPermissionsByTemplateId(templateId);
	}

	public Map<String, Collection<SecurityGroupPermission>> getAllSecurityGroups() {
		verifyAndReturnUser();
		return securityGroupManager.getAllSecurityGroupPermissions();
	}

	public void authorizeSecurityGroupsByKey(String templateId, SecurityGroupPermission sgp) {
		verifyAndReturnUser();
		String groupId = securityGroupManager.getSecurityGroupIdByTemplateId(templateId);
		sgp.setSecurityGroupId(groupId);
		securityGroupManager.authorizeSecurityGroup(groupId, sgp);
	}

	public void revokeSecurityGroupsByKey(String templateId, SecurityGroupPermission sgp) {
		verifyAndReturnUser();
		String groupId = securityGroupManager.getSecurityGroupIdByTemplateId(templateId);
		sgp.setSecurityGroupId(groupId);
		securityGroupManager.revokeSecurityGroup(groupId, sgp);
	}

	public String securityGroupIdByTemplateId(String templateId) {
		verifyAndReturnUser();
		String groupId = securityGroupManager.getSecurityGroupIdByTemplateId(templateId);
		return groupId;
	}

	public void deleteSecurityGroup(String groupId) {
		verifyAndReturnUser();
		securityGroupManager.removeSecurityGroup(groupId);
	}

	// public Map<String, Collection<SecurityGroupPermission>>
	// getAllSecurityGroups() {
	// verifyAndReturnUser();
	// return securityGroupManager.getAllSecurityGroupPermissions();
	// }

	public Iterable<Printer> getAllPrinters() {
		verifyAndReturnUser();
		return resourceManager.getAllPrinters();
	}

	public Iterable<FileSystem> getAllFileSystems() {
		verifyAndReturnUser();
		return resourceManager.getAllFileSystems();
	}

	public Iterable<Printer> getPrintersForVirtueTemplate(String virtueTemplateId) {
		verifyAndReturnUser();
		VirtueTemplate virtueTemplate = templateManager.getVirtueTemplate(virtueTemplateId);
		Map<String, Printer> printers = resourceManager.getPrintersForVirtueTemplate(virtueTemplate);
		return printers.values();
	}

	public void clearPrinters() {
		verifyAndReturnUser();
		resourceManager.clear();
	}

	public void setXenPoolSize(int poolSize) {
		xenVmProvider.setXenPoolSize(poolSize);
	}

	public int getXenPoolSize() {
		return xenVmProvider.getXenPoolSize();
	}

}

