package com.ncc.savior.virtueadmin.rest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java.io.File;
import org.apache.commons.io.FileUtils;
import java.nio.charset.StandardCharsets;

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

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.ncc.savior.util.SaviorErrorCode;
import com.ncc.savior.util.SaviorException;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.ClipboardPermission;
import com.ncc.savior.virtueadmin.model.ClipboardPermissionOption;
import com.ncc.savior.virtueadmin.model.IconModel;
import com.ncc.savior.virtueadmin.model.SecurityGroupPermission;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VirtualMachineTemplate;
import com.ncc.savior.virtueadmin.model.VirtueInstance;
import com.ncc.savior.virtueadmin.model.VirtuePersistentStorage;
import com.ncc.savior.virtueadmin.model.VirtueSession;
import com.ncc.savior.virtueadmin.model.VirtueTemplate;
import com.ncc.savior.virtueadmin.model.VirtueUser;
import com.ncc.savior.virtueadmin.model.Printer;
import com.ncc.savior.virtueadmin.model.FileSystem;
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
	private static final Logger logger = LoggerFactory.getLogger(AdminResource.class);

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
	@Path("application/create")
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
	@Path("virtualMachine/template/create")
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

	@PUT
	@Produces("application/json")
	@Path("virtualMachine/template/{id}/setStatus")
	public VirtualMachineTemplate setVirtualMachineTemplateStatus(@PathParam("id") String templateId, String status) {
		boolean newStatus = Boolean.parseBoolean(status);
		VirtualMachineTemplate virtualMachineTemplate = adminService.setVirtualMachineTemplateStatus(templateId, newStatus);
		return virtualMachineTemplate;
	}

	@POST
	@Produces("application/json")
	@Path("virtue/template/create")
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

	@PUT
	@Produces("application/json")
	@Path("virtue/template/{id}/setStatus")
	public VirtueTemplate setVirtueTemplateStatus(@PathParam("id") String templateId, String newStatus) {
		boolean status = Boolean.parseBoolean(newStatus);
		VirtueTemplate virtueTemplate = adminService.setVirtueTemplateStatus(templateId, status);
		return virtueTemplate;
	}

	@PUT
	@Produces("application/json")
	@Path("virtue/template/{id}")
	public VirtueTemplate updateVirtueTemplate(@PathParam("id") String templateId, VirtueTemplate template) {
		logger.debug("\n" + template.getFileSystems().size());
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
	@Path("user/create")
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

	@PUT
	@Produces("application/json")
	@Path("user/{username}/setStatus")
	public void setUserStatus(@PathParam("username") String username, String newStatus) {
		boolean status = Boolean.parseBoolean(newStatus);
		adminService.enableDisableUser(username, status);
	}

	// JHU - Admin API - user role authorize
	@POST // #TODO PUT? Is this idempotent? What in here should prevent duplicates?
	@Produces("application/json")
	@Path("user/{username}/assign/{templateId}")
	public void assignTemplateToUser(@PathParam("username") String username,
			@PathParam("templateId") String templateId) {
		adminService.assignTemplateToUser(templateId, username);
	}

	// JHU - Admin API - user role unauthorize
	@POST // #TODO PUT
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
				importExportService.exportDatabaseWithoutImages(os);
				os.flush();
			}
		};
		return Response.ok(stream).build();
	}

	@GET
	@Path("export/user")
	@Produces("application/zip")
	public Response exportAllUsers() {
		StreamingOutput stream = new StreamingOutput() {
			@Override
			public void write(OutputStream os) throws IOException, WebApplicationException {
				importExportService.exportZippedAllUsers(os);
				os.flush();
			}
		};
		return Response.ok(stream).header("Content-Disposition", "attachment; filename=\"virtue-" + "users" + ".zip\"")
				.build();
	}

	@GET
	@Path("export/user/{username}")
	@Produces("application/zip")
	public Response exportUser(@PathParam("username") String username) {
		StreamingOutput stream = new StreamingOutput() {
			@Override
			public void write(OutputStream os) throws IOException, WebApplicationException {
				importExportService.exportZippedUser(username, os);
				os.flush();
			}
		};
		return Response.ok(stream).header("Content-Disposition", "attachment; filename=\"virtue-" + username + ".zip\"")
				.build();
	}

	@GET
	@Path("export/virtue/template/{templateId}")
	@Produces("application/zip")
	public Response exportVirtueTemplate(@PathParam("templateId") String templateId) {
		StreamingOutput stream = new StreamingOutput() {
			@Override
			public void write(OutputStream os) throws IOException, WebApplicationException {
				try {
					importExportService.exportZippedVirtueTemplate(templateId, os);
					os.flush();
				} catch (Throwable t) {
					logger.debug("", t);
				}
			}
		};
		return Response.ok(stream)
				.header("Content-Disposition", "attachment; filename=\"virtue-" + templateId + ".zip\"").build();
	}

	@GET
	@Path("export/all")
	@Produces("application/zip")
	public Response exportSystemZipped() {
		StreamingOutput stream = new StreamingOutput() {
			@Override
			public void write(OutputStream os) throws IOException, WebApplicationException {
				importExportService.exportZippedAll(os);
				os.flush();
			}
		};
		return Response.ok(stream).header("Content-Disposition", "attachment; filename=\"allVirtueTemplates.zip\"")
				.build();
	}

	@GET
	@Path("export/virtue/template")
	@Produces("application/zip")
	public Response exportAllVirtuesZipped() {
		StreamingOutput stream = new StreamingOutput() {
			@Override
			public void write(OutputStream os) throws IOException, WebApplicationException {
				importExportService.exportZippedAllTemplates(os);
				os.flush();
			}
		};
		return Response.ok(stream).header("Content-Disposition", "attachment; filename=\"allVirtueTemplates.zip\"")
				.build();
	}

	@GET
	@Path("export/virtualMachine/template/{templateId}")
	@Produces("application/zip")
	public Response exportVirtualMachineZipped(@PathParam("templateId") String templateId) {
		StreamingOutput stream = new StreamingOutput() {
			@Override
			public void write(OutputStream os) throws IOException, WebApplicationException {
				importExportService.exportZippedVirtualMachineTemplate(templateId, os);
				os.flush();
			}
		};
		return Response.ok(stream).header("Content-Disposition", "attachment; filename=\"vm-" + templateId + ".zip\"")
				.build();
	}

	@POST
	@Path("import")
	@Produces("application/json")
	@Consumes("application/json")
	public void importSystem(InputStream stream) {
		importExportService.importSystemDatabaseWithoutImages(stream);
	}

	@POST
	@Path("import")
	@Produces("application/json")
	@Consumes({ "application/zip", "application/octet-stream" })
	public void importZip(InputStream stream,
			@QueryParam("waitForCompletion") @DefaultValue("true") boolean waitForCompletion) {
		importExportService.importZip(stream, waitForCompletion);
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
			String response = adminService.getSensingResponse();
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

	/**
	 * Retrieves all the available permissions. If the query parameter 'raw' is set
	 * to true, only permissions stored in the database are returned. Otherwise, all
	 * returnable computed permissions are returned. This typically is a permission
	 * for every possible pair of virtue template ID to every other virtue template
	 * ID in both directions and every virtue template ID to the default setting.
	 *
	 * @param raw
	 * @return
	 */
	@GET
	@Path("permissions")
	@Produces("application/json")
	public Iterable<ClipboardPermission> getAllPermissions(@DefaultValue("false") @QueryParam("raw") boolean raw) {
		if (raw) {
			Iterable<ClipboardPermission> p = permissionService.getAllRawPermissions();
			return p;
		} else {
			Collection<String> sourceIds = getAllSourceIds();
			return permissionService.getAllPermissionsForSources(sourceIds);
		}
	}

	/**
	 * Set a new value for a permission. The value should be sent as the body of the
	 * POST.
	 *
	 * @param sourceId
	 * @param destId
	 * @param optionStr
	 * @return
	 */
	@POST // #TODO PUT
	@Path("permission/{sourceId}/{destId}")
	public String setPermission(@PathParam("sourceId") String sourceId, @PathParam("destId") String destId,
			String optionStr) {
		ClipboardPermissionOption option = ClipboardPermissionOption.valueOf(optionStr);
		permissionService.setClipboardPermission(sourceId, destId, option);
		return "Success, go back and refresh";
	}

	/**
	 * Retrieves a permission from the system. By default, the permission will be
	 * computed taking into account any defaults when a more specific setting cannot
	 * be found. To get just the raw permission that is stored in the data, set the
	 * query parameter 'raw' to true.
	 *
	 * @param sourceId
	 * @param destId
	 * @param raw
	 * @return
	 */
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

	/**
	 * Get list of permissions for a given source ID. If the query parameter 'raw'
	 * is set to true, only permissions stored in the database are returned.
	 * Otherwise, all returnable computed permissions are returned.
	 *
	 * @param sourceId
	 * @param raw
	 * @return
	 */
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

	/**
	 * Clear a permission from the system if it exists. Once this operation occurs,
	 * the computed permission should return a default value either from the source
	 * ID or the system.
	 *
	 * @param sourceId
	 * @param destId
	 */
	@DELETE
	@Path("permission/{sourceId}/{destId}")
	public void clearPermission(@PathParam("sourceId") String sourceId, @PathParam("destId") String destId) {
		permissionService.clearClipboardPermission(sourceId, destId);
	}

	/**
	 * Returns the system default {@link ClipboardPermissionOption}.
	 *
	 * @param optionStr
	 */
	@GET
	@Path("permission/default/{option}")
	public void setServiceDefaultPermission(@PathParam("option") String optionStr) {
		ClipboardPermissionOption option = ClipboardPermissionOption.valueOf(optionStr);
		permissionService.setDefaultClipboardPermission(option);
	}

	@GET
	@Path("storage")
	@Produces("application/json")
	public Iterable<VirtuePersistentStorage> getAllStorage() {
		return adminService.getAllPersistentStorage();
	}

	@GET
	@Path("storage/{user}/")
	@Produces("application/json")
	public Iterable<VirtuePersistentStorage> getPersistentStorageForUser(@PathParam("user") String username) {
		return adminService.getPersistentStorageForUser(username);
	}

	@GET
	@Path("storage/{user}/{virtueTemplateId}")
	@Produces("application/json")
	public VirtuePersistentStorage getPersistentStorage(@PathParam("user") String username,
			@PathParam("virtueTemplateId") String virtueTemplateId) {
		return adminService.getPersistentStorage(username, virtueTemplateId);
	}

	@DELETE
	@Path("storage/{user}/{virtueTemplateId}")
	@Produces("application/json")
	public void deletePersistentStorage(@PathParam("user") String username,
			@PathParam("virtueTemplateId") String virtueTemplateId) {
		adminService.deletePersistentStorage(username, virtueTemplateId);
	}

	@GET
	@Path("securityGroup")
	@Produces("application/json")
	public Map<String, Collection<SecurityGroupPermission>> getAllSecurityGroupsAndPermissions() {
		return adminService.getAllSecurityGroups();
	}

	@GET
	@Path("securityGroup/test")
	@Produces("application/json")
	public void create() {
		SecurityGroupPermission sgp = new SecurityGroupPermission(true, 80, 80, "192.168.1.0/32", "tcp", "test");
		adminService.authorizeSecurityGroupsByKey("test", sgp);
	}

	@GET
	@Path("securityGroup/id/{groupId}")
	@Produces("application/json")
	public Collection<SecurityGroupPermission> getPermissionsByGroupId(@PathParam("groupId") String groupId) {
		return adminService.getSecurityGroupPermissions(groupId);
	}

	@GET
	@Path("securityGroup/template/{templateId}")
	@Produces("application/json")
	public Collection<SecurityGroupPermission> getGroupIdByTemplate(@PathParam("templateId") String templateId) {
		return adminService.getSecurityGroupPermissionsByTemplate(templateId);
	}

	@POST // #TODO PUT
	@Path("securityGroup/template/{templateId}/revoke")
	public void revokePermissionForTemplate(@PathParam("templateId") String templateId, SecurityGroupPermission sgp) {
		adminService.revokeSecurityGroupsByKey(templateId, sgp);
	}

	@POST  // #TODO PUT?
	@Path("securityGroup/template/{templateId}/authorize")
	public void authorizePermissionFromTemplate(@PathParam("templateId") String templateId,
			SecurityGroupPermission sgp) {
		adminService.authorizeSecurityGroupsByKey(templateId, sgp);
	}

	@DELETE
	@Path("securityGroup/template/{templateId}")
	public void deleteByTemplateId(@PathParam("templateId") String templateId) {
		String secGroupId = adminService.securityGroupIdByTemplateId(templateId);
		adminService.deleteSecurityGroup(secGroupId);
	}

	@DELETE
	@Path("securityGroup/id/{groupId}")
	public void deleteByGroupId(@PathParam("groupId") String groupId) {
		adminService.deleteSecurityGroup(groupId);
	}

	private Collection<String> getAllSourceIds() {
		Iterable<VirtueTemplate> templates = adminService.getAllVirtueTemplates();
		Collection<String> sourceIds = new ArrayList<String>();
		for (VirtueTemplate t : templates) {
			sourceIds.add(t.getId());
		}
		sourceIds.add(ClipboardPermission.DESKTOP_CLIENT_GROUP_ID);
		return sourceIds;
	}

/*************/

	@POST
	@Produces("application/json")
	@Path("printer/create")
	public Printer createPrinter(Printer printer) {
		return adminService.createPrinter(printer);
	}

	@PUT
	@Produces("application/json")
	@Path("printer/{printerId}")
	public Printer updatePrinter(@PathParam("printerId") String printerId, Printer printer) {
		if (!printer.getId().equals(printerId)) {
			throw new SaviorException(SaviorErrorCode.ID_MISMATCH,
					"Given printer doesn't match printerId in path. Printer ID=" + printerId + ". Printer=" + printer);
		}
		return adminService.updatePrinter(printerId, printer);
	}

	@GET
	@Produces("application/json")
	@Path("printer/{printerId}")
	public Printer getPrinter(@PathParam("printerId") String printerId) {
		Printer returnedPrinter = adminService.getPrinter(printerId);
		return returnedPrinter;
	}

	// JHU - Admin API - user list
	@GET
	@Produces("application/json")
	@Path("printer")
	public Iterable<Printer> getAllPrinters() {
		return adminService.getAllPrinters();

		// ArrayList<Printer> ps = new ArrayList<Printer>();
		// ps.add(new Printer("id", "name", "address", "status", true));
		// return ps;
		// return adminService.getAllPrinters();
	}

	// JHU - Admin API - user get
	@DELETE
	@Produces("application/json")
	@Path("printer/{printerId}")
	public void deletePrinter(@PathParam("printerId") String printerId) {
		adminService.deletePrinter(printerId);
	}

	@PUT
	@Produces("application/json")
	@Path("printer/{printerId}/setStatus")
	public void setPrinterStatus(@PathParam("printerId") String printerId, String enableString) {
		boolean enable = Boolean.parseBoolean(enableString);
		adminService.setPrinterStatus(printerId, enable);
	}
	/********************/

	@POST
	@Produces("application/json")
	@Path("fileSystem/create")
	public FileSystem createFileSystem(FileSystem fileSystem) {
		return adminService.createFileSystem(fileSystem);
	}

	@PUT
	@Produces("application/json")
	@Path("fileSystem/{fileSystemId}")
	public FileSystem updateFileSystem(@PathParam("fileSystemId") String fileSystemId, FileSystem fileSystem) {
		if (!fileSystem.getId().equals(fileSystemId)) {
			throw new SaviorException(SaviorErrorCode.ID_MISMATCH,
					"Given fileSystem doesn't match input id. FileSystem ID=" + fileSystemId + ". FileSystem=" + fileSystem);
		}
		return adminService.updateFileSystem(fileSystemId, fileSystem);
	}

	@GET
	@Produces("application/json")
	@Path("fileSystem/{fileSystemId}")
	public FileSystem getFileSystem(@PathParam("fileSystemId") String fileSystemId) {
		FileSystem returnedFileSystem = adminService.getFileSystem(fileSystemId);
		return returnedFileSystem;
	}

	@GET
	@Produces("application/json")
	@Path("fileSystem")
	public Iterable<FileSystem> getAllFileSystems() {
		// ArrayList<FileSystem> ps = new ArrayList<FileSystem>();
		// ps.add(new FileSystem("id", "name", "address", "status", true));
		// return ps;
		return adminService.getAllFileSystems();
	}

	@DELETE
	@Produces("application/json")
	@Path("fileSystem/{fileSystemId}")
	public void deleteFileSystem(@PathParam("fileSystemId") String fileSystemId) {
		adminService.deleteFileSystem(fileSystemId);
	}

	@PUT
	@Produces("application/json")
	@Path("fileSystem/{fileSystemId}/setStatus")
	public void setFileSystemStatus(@PathParam("fileSystemId") String fileSystemId, String enableString) {
		boolean enable = Boolean.parseBoolean(enableString);
		adminService.setFileSystemStatus(fileSystemId, enable);
	}

}
