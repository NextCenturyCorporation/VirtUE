package com.ncc.savior.virtueadmin.rest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.lang3.tuple.Pair;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.beans.factory.annotation.Autowired;

import com.ncc.savior.util.SaviorErrorCode;
import com.ncc.savior.util.SaviorException;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.ClipboardPermission;
import com.ncc.savior.virtueadmin.model.ClipboardPermissionOption;
import com.ncc.savior.virtueadmin.model.IconModel;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VirtualMachineTemplate;
import com.ncc.savior.virtueadmin.model.VirtueInstance;
import com.ncc.savior.virtueadmin.model.VirtueSession;
import com.ncc.savior.virtueadmin.model.VirtueTemplate;
import com.ncc.savior.virtueadmin.model.VirtueUser;
import com.ncc.savior.virtueadmin.service.AdminService;
import com.ncc.savior.virtueadmin.service.DesktopVirtueService;
import com.ncc.savior.virtueadmin.service.ImportExportService;
import com.ncc.savior.virtueadmin.service.PermissionService;
import com.ncc.savior.virtueadmin.util.WebServiceUtil;

/**
 * Rest resource that handles endpoints specifically for an administrator
 * 
 */

@Path("/admin")
public class AdminResource {

	@Autowired
	private DesktopVirtueService desktopService;

	@Autowired
	private AdminService adminService;

	@Autowired
	private ImportExportService importExportService;

	@Autowired
	private PermissionService permissionService;

	public AdminResource() {

	}

	// JHU - Admin API unimplemented:
	// resourse get
	// resource list
	// resource attach
	// resource detach
	// system export
	// system import
	// test import user
	// test import application
	// test import role

	@POST
	@Produces("application/json")
	@Path("application")
	public ApplicationDefinition createNewApplicationDefinition(ApplicationDefinition appDef) {
		return adminService.createNewApplicationDefinition(appDef);
	}

	// JHU - Admin API - application list
	@GET
	@Produces("application/json")
	@Path("application")
	public Iterable<ApplicationDefinition> getAllApplicationDefinitions() {
		return adminService.getAllApplicationTemplates();
	}

	@GET
	@Produces("application/json")
	@Path("application/{id}")
	public ApplicationDefinition getApplicationDefinition(@PathParam("id") String templateId) {
		return adminService.getApplicationDefinition(templateId);
	}

	@PUT
	@Produces("application/json")
	@Path("application/{templateId}")
	public ApplicationDefinition updateApplicationDefinitions(@PathParam("templateId") String templateId,
			ApplicationDefinition appDef) {
		return adminService.updateApplicationDefinitions(templateId, appDef);
	}

	@PUT
	// @Consumes({ "image/png", "image/jpeg" })
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Path("icon/{iconKey}")
	public void uploadIcon(@PathParam("iconKey") String iconKey, @FormDataParam("file") InputStream uploadedInputStream,
			@FormDataParam("file") FormDataContentDisposition fileDetail) {
		adminService.uploadIcon(iconKey, uploadedInputStream);
	}

	@GET
	@Produces({ "image/png", "image/jpeg" })
	@Path("icon/{iconKey}")
	public byte[] getIcon(@PathParam("iconKey") String iconKey) {
		IconModel iconModel = adminService.getIcon(iconKey);
		return iconModel.getData();
	}

	@GET
	@Produces("application/json")
	@Path("icon")
	public Set<String> getAllIconKeys() {
		Set<String> keys = adminService.getAllIconKeys();
		return keys;
	}

	@DELETE
	@Path("icon/{iconKey}")
	public void deleteIcon(@PathParam("iconKey") String iconKey) {
		adminService.deleteIcon(iconKey);
	}

	@DELETE
	@Path("application/{id}")
	public void deleteApplicationDefinitions(@PathParam("id") String templateId) {
		adminService.deleteApplicationDefinition(templateId);
	}

	// JHU - Admin API - role create
	@POST
	@Produces("application/json")
	@Path("virtualMachine/template/")
	public VirtualMachineTemplate createVmTemplate(VirtualMachineTemplate templateId) {
		return adminService.createVmTemplate(templateId);
	}

	@GET
	@Produces("application/json")
	@Path("virtualMachine/template/{id}")
	public VirtualMachineTemplate getVmTemplate(@PathParam("id") String templateId) {
		return adminService.getVmTemplate(templateId);
	}

	// JHU - Admin API - role list
	@GET
	@Produces("application/json")
	@Path("virtualMachine/template")
	public Iterable<VirtualMachineTemplate> getAllVmTemplates() {
		return adminService.getAllVmTemplates();
	}

	@PUT
	@Produces("application/json")
	@Path("virtualMachine/template/{id}")
	public VirtualMachineTemplate updateVmTemplate(@PathParam("id") String templateId, VirtualMachineTemplate vmt) {
		return adminService.updateVmTemplate(templateId, vmt);
	}

	@DELETE
	@Path("virtualMachine/template/{id}")
	public void deleteVmTemplate(@PathParam("id") String templateId) {
		adminService.deleteVmTemplate(templateId);
	}

	@GET
	@Produces("application/json")
	@Path("virtualMachine/template/{id}/toggle")
	public VirtualMachineTemplate toggleVirtualMachineTemplateEnabled(@PathParam("id") String templateId) {
		VirtualMachineTemplate virtualMachineTemplate = adminService.toggleVirtualMachineTemplateEnabled(templateId);
		return virtualMachineTemplate;
	}

	@POST
	@Produces("application/json")
	@Path("virtue/template")
	public VirtueTemplate createNewVirtueTemplate(VirtueTemplate template) {
		VirtueTemplate virtueTemplate = adminService.createNewVirtueTemplate(template);
		return virtueTemplate;
	}

	/**
	 * Gets all {@link VirtueTemplate}s in the system
	 * 
	 * @return
	 * 
	 */
	@GET
	@Produces("application/json")
	@Path("virtue/template")
	public Iterable<VirtueTemplate> getAllVirtueTemplates(@QueryParam("user") String user) {
		if (user == null || user.trim().equals("")) {
			Iterable<VirtueTemplate> virtueTemplates = adminService.getAllVirtueTemplates();
			return virtueTemplates;
		} else {
			Iterable<VirtueTemplate> virtueTemplates = adminService.getVirtueTemplatesForUser(user);
			return virtueTemplates;
		}
	}

	@GET
	@Produces("application/json")
	@Path("virtue/template/{id}")
	public VirtueTemplate getVirtueTemplate(@PathParam("id") String templateId) {
		VirtueTemplate virtueTemplate = adminService.getVirtueTemplate(templateId);
		return virtueTemplate;
	}

	@GET
	@Produces("application/json")
	@Path("virtue/template/{id}/toggle")
	public VirtueTemplate toggleVirtueTemplateEnabled(@PathParam("id") String templateId) {
		VirtueTemplate virtueTemplate = adminService.toggleVirtueTemplateEnabled(templateId);
		return virtueTemplate;
	}

	@PUT
	@Produces("application/json")
	@Path("virtue/template/{id}")
	public VirtueTemplate updateVirtueTemplate(@PathParam("id") String templateId, VirtueTemplate template) {
		VirtueTemplate virtueTemplate = adminService.updateVirtueTemplate(templateId, template);
		return virtueTemplate;
	}

	@DELETE
	@Produces("application/json")
	@Path("virtue/template/{id}")
	public void deleteVirtueTemplate(@PathParam("id") String templateId) {
		adminService.deleteVirtueTemplate(templateId);
	}

	/**
	 * Starts the given application after provisioning a new virtue from the given
	 * template.
	 * 
	 * @param templateId
	 * @param applicationId
	 * @return
	 */
	@GET
	@Produces("application/json")
	@Path("createvirtue/type/{templateId}")
	public VirtueInstance createVirtueFromTemplate(@PathParam("templateId") String templateId) {
		return desktopService.createVirtue(templateId);
	}

	// JHU - Admin API - user virtue list
	@GET
	@Produces("application/json")
	@Path("virtues")
	public Iterable<VirtueInstance> getAllActiveVirtues(@QueryParam("user") String username) {
		if (username == null || username.trim().equals("")) {
			return adminService.getAllActiveVirtues();
		} else {
			return adminService.getAllActiveVirtuesForUser(username);
		}
	}

	@GET
	@Produces("application/json")
	@Path("virtues/{id}")
	public VirtueInstance getActiveVirtue(@PathParam("id") String virtueId) {
		return adminService.getActiveVirtue(virtueId);
	}

	@GET
	@Path("deletevirtue/instance/{instanceId}")
	public void deleteVirtue(@PathParam("instanceId") String instanceId) {
		adminService.deleteVirtue(instanceId);
	}

	@POST
	@Produces("application/json")
	@Path("user/")
	public VirtueUser createUpdateUser(VirtueUser newUser) {
		return adminService.createUpdateUser(newUser);
	}

	@PUT
	@Produces("application/json")
	@Path("user/{username}")
	public VirtueUser updateUser(@PathParam("username") String username, VirtueUser newUser) {
		if (!newUser.getUsername().equals(username)) {
			throw new SaviorException(SaviorErrorCode.ID_MISMATCH,
					"Given user doesn't match username in path.  Username=" + username + ". NewUser=" + newUser);
		}
		return adminService.createUpdateUser(newUser);
	}

	@GET
	@Produces("application/json")
	@Path("user/{username}")
	public VirtueUser getUser(@PathParam("username") String usernameToRetrieve) {
		VirtueUser returnedUser = adminService.getUser(usernameToRetrieve);
		return returnedUser;
	}

	// JHU - Admin API - user list
	@GET
	@Produces("application/json")
	@Path("user")
	public Iterable<VirtueUser> getAllUsers() {
		return adminService.getAllUsers();
	}

	// JHU - Admin API - user get
	@DELETE
	@Produces("application/json")
	@Path("user/{username}")
	public void removeUser(@PathParam("username") String usernameToRemove) {
		adminService.removeUser(usernameToRemove);
	}

	@POST
	@Produces("application/json")
	@Path("user/{username}/enable")
	public void removeUser(@PathParam("username") String username, String enableString) {
		boolean enable = Boolean.parseBoolean(enableString);
		adminService.enableDisableUser(username, enable);
	}

	// JHU - Admin API - user role authorize
	@POST
	@Produces("application/json")
	@Path("user/{username}/assign/{templateId}")
	public void assignTemplateToUser(@PathParam("username") String username,
			@PathParam("templateId") String templateId) {
		adminService.assignTemplateToUser(templateId, username);
	}

	// JHU - Admin API - user role unauthorize
	@POST
	@Produces("application/json")
	@Path("user/{username}/revoke/{templateId}")
	public void revokeTemplateToUser(@PathParam("username") String username,
			@PathParam("templateId") String templateId) {
		adminService.revokeTemplateFromUser(templateId, username);
	}

	@GET
	@Path("user/active")
	@Produces("application/json")
	public Iterable<VirtueUser> getActiveUsers() {
		return adminService.getActiveUsers();
	}

	// JHU - Admin API - user logout
	@GET
	@Path("user/{username}/logout")
	@Produces("application/json")
	public void logoutUser(@PathParam("username") String username) {
		adminService.logoutUser(username);
	}

	// JHU - Admin API - usertoken list
	@GET
	@Path("session")
	@Produces("application/json")
	public Map<String, List<String>> getAllSessions() {
		return adminService.getActiveSessions();
	}

	@GET
	@Path("session/{sessionId}")
	@Produces("application/json")
	public VirtueSession getSession(@PathParam("sessionId") String sessionId) {
		return adminService.getActiveSession(sessionId);
	}

	@DELETE
	@Path("session/{sessionId}")
	@Produces("application/json")
	public void invalidateSession(@PathParam("sessionId") String sessionId) {
		adminService.invalidateSession(sessionId);
	}

	@GET
	@Path("export")
	@Produces("application/json")
	public Response exportSystem() {
		StreamingOutput stream = new StreamingOutput() {
			@Override
			public void write(OutputStream os) throws IOException, WebApplicationException {
				importExportService.exportSystem(os);
				os.flush();
			}
		};
		return Response.ok(stream).build();
	}

	@POST
	@Path("import")
	@Produces("application/json")
	public void importSystem(InputStream stream) {
		importExportService.importSystem(stream);
	}

	@GET
	@Path("import/{type}/{name}")
	@Produces("text/plain")
	public String importItem(@PathParam("type") String type, @PathParam("name") String name) {
		switch (type) {
		case ImportExportService.TYPE_APPLICATION:
			ApplicationDefinition app = importExportService.importApplication(name);
			return app.getId();
		case ImportExportService.TYPE_VIRTUAL_MACHINE:
			VirtualMachineTemplate vmt = importExportService.importVirtualMachineTemplate(name);
			return vmt.getId();
		case ImportExportService.TYPE_VIRTUE:
			VirtueTemplate vt = importExportService.importVirtueTemplate(name);
			return vt.getId();
		case ImportExportService.TYPE_USER:
			VirtueUser user = importExportService.importUser(name);
			return user.getUsername();
		default:
			throw WebServiceUtil.createWebserviceException(
					new SaviorException(SaviorErrorCode.IMPORT_NOT_FOUND, "Invalid import type=" + type));
		}
	}

	@GET
	@Path("import/all")
	@Produces("text/plain")
	public String importAll() {
		int items = importExportService.importAll();
		return "imported " + items + " items.";
	}

	@GET
	@Path("sensing")
	@Produces("application/json")
	public Response getSensing() throws IOException {
		try {
			String response = adminService.getSensingReponse();
			return Response.ok(response).build();
		} catch (Exception e) {
			throw WebServiceUtil.createWebserviceException(e);
		}

	}

	@GET
	@Path("vm")
	@Produces("application/json")
	public Iterable<VirtualMachine> getAllVms() {
		try {
			return adminService.getAllVirtualMachines();
		} catch (Exception e) {
			throw WebServiceUtil.createWebserviceException(e);
		}
	}

	@GET
	@Path("vm/{id}")
	@Produces("application/json")
	public VirtualMachine getVm(@PathParam("id") String id) {
		try {
			return adminService.getVm(id);
		} catch (Exception e) {
			throw WebServiceUtil.createWebserviceException(e);
		}
	}

	@GET
	@Path("vm/reboot/{id}")
	@Produces("application/json")
	public void rebootVm(@PathParam("id") String vmId) {
		try {
			adminService.rebootVm(vmId);
		} catch (Exception e) {
			throw WebServiceUtil.createWebserviceException(e);
		}
	}

	@GET
	@Path("permissions/raw")
	@Produces("application/json")
	public Iterable<ClipboardPermission> getAllRawPermissions() {
		Iterable<ClipboardPermission> p = permissionService.getAllRawPermissions();
		return p;
	}

	@GET
	@Path("permissions/computed")
	@Produces("application/json")
	public Map<Pair<String, String>, ClipboardPermissionOption> getAllComputedPermissions() {
		Collection<String> sourceIds = getAllSourceIds();
		return permissionService.getAllPermissionsForSourcesAsMap(sourceIds);
	}

	@POST
	@Path("permission/{sourceId}/{destId}")
	public String setPermission(@PathParam("sourceId") String sourceId, @PathParam("destId") String destId,
			String optionStr) {
		ClipboardPermissionOption option = ClipboardPermissionOption.valueOf(optionStr);
		permissionService.setClipboardPermission(sourceId, destId, option);
		return "Success, go back and refresh";
	}

	@GET
	@Produces("application/json")
	@Path("permission/{sourceId}/{destId}")
	public ClipboardPermission getPermission(@PathParam("sourceId") String sourceId, @PathParam("destId") String destId,
			@DefaultValue("false") @QueryParam("raw") boolean raw) {
		ClipboardPermission p;
		if (raw) {
			p = permissionService.getRawClipboardPermission(sourceId, destId);
		} else {
			p = permissionService.getClipboardPermission(sourceId, destId);
		}
		if (p == null) {
			throw new SaviorException(SaviorErrorCode.PERMISSION_NOT_FOUND, (raw ? "Raw c" : "C")
					+ "lipboard permission for source=" + sourceId + " and dest=" + destId + " was not found");
		}
		return p;
	}

	@GET
	@Produces("application/json")
	@Path("permission/{sourceId}")
	public List<ClipboardPermission> getPermissionsForSource(@PathParam("sourceId") String sourceId,
			@DefaultValue("false") @QueryParam("raw") boolean raw) {
		List<ClipboardPermission> p;
		if (raw) {
			p = permissionService.getRawClipboardPermissionForSource(sourceId);
		} else {
			Collection<String> sourceIds = getAllSourceIds();
			p = permissionService.getClipboardPermissionForSource(sourceId, sourceIds);
		}
		return p;
	}

	@DELETE
	@Path("permission/{sourceId}/{destId}")
	public void clearPermission(@PathParam("sourceId") String sourceId, @PathParam("destId") String destId) {
		permissionService.clearClipboardPermission(sourceId, destId);
	}

	@GET
	@Path("permission/default/{option}")
	public void setServiceDefaultPermission(@PathParam("option") String optionStr) {
		ClipboardPermissionOption option = ClipboardPermissionOption.valueOf(optionStr);
		permissionService.setDefaultClipboardPermission(option);
	}

	private Collection<String> getAllSourceIds() {
		Iterable<VirtueTemplate> templates = adminService.getAllVirtueTemplates();
		Collection<String> sourceIds = new ArrayList<String>();
		for (VirtueTemplate t : templates) {
			sourceIds.add(t.getId());
		}
		sourceIds.add(ClipboardPermission.DESKTOP_CLIENT_ID);
		return sourceIds;
	}
}
