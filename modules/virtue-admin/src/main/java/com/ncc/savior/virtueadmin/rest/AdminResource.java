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
import com.ncc.savior.virtueadmin.model.FileSystem;
import com.ncc.savior.virtueadmin.model.IconModel;
import com.ncc.savior.virtueadmin.model.Printer;
import com.ncc.savior.virtueadmin.model.SecurityGroupPermission;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VirtualMachineTemplate;
import com.ncc.savior.virtueadmin.model.VirtueInstance;
import com.ncc.savior.virtueadmin.model.VirtuePersistentStorage;
import com.ncc.savior.virtueadmin.model.VirtueSession;
import com.ncc.savior.virtueadmin.model.VirtueTemplate;
import com.ncc.savior.virtueadmin.model.VirtueUser;
import com.ncc.savior.virtueadmin.service.AdminService;
import com.ncc.savior.virtueadmin.service.DesktopVirtueService;
import com.ncc.savior.virtueadmin.service.ImportExportService;
import com.ncc.savior.virtueadmin.service.PermissionService;
import com.ncc.savior.virtueadmin.util.WebServiceUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;

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
	@Path("application")
	@Operation(summary = "Create new application", description = "Creates and returns and new application definition.  Some values in the ApplicationDefinition structure will be ignored I.E. id, updated and created entries.")
	public ApplicationDefinition createNewApplicationDefinition(ApplicationDefinition appDef) {
		return adminService.createNewApplicationDefinition(appDef);
	}

	// JHU - Admin API - application list
	@GET
	@Produces("application/json")
	@Path("application")
	@Operation(summary = "Get all application definitions in the system.", description = "Returns a list of all the application definitions in the system.")
	public Iterable<ApplicationDefinition> getAllApplicationDefinitions() {
		return adminService.getAllApplicationTemplates();
	}

	@GET
	@Produces("application/json")
	@Path("application/{id}")
	@Operation(summary = "Get application definition", description = "Gets a specific application definition based on the ID in the path.")
	public ApplicationDefinition getApplicationDefinition(@PathParam("id") String templateId) {
		return adminService.getApplicationDefinition(templateId);
	}

	@PUT
	@Produces("application/json")
	@Path("application/{templateId}")
	@Operation(summary = "Update an application definition", description = "Pass in an application defintion to update an existing application definition.  Sending the ID in the structure is not necessary.")
	public ApplicationDefinition updateApplicationDefinitions(@PathParam("templateId") String templateId,
			ApplicationDefinition appDef) {
		return adminService.updateApplicationDefinitions(templateId, appDef);
	}

	@PUT
	// @Consumes({ "image/png", "image/jpeg" })
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Path("icon/{iconKey}")
	@Operation(summary = "Upload an application icon", description = "Uploads an application icon to a given icon key.  The icon key here needs to match the applications icon key.")
	public void uploadIcon(@PathParam("iconKey") String iconKey, @FormDataParam("file") InputStream uploadedInputStream,
			@FormDataParam("file") FormDataContentDisposition fileDetail) {
		adminService.uploadIcon(iconKey, uploadedInputStream);
	}

	@GET
	@Produces({ "image/png", "image/jpeg" })
	@Path("icon/{iconKey}")
	@Operation(summary = "Get application icon.", description = "Returns an application icon associated with the given icon key.  If the icon key does not exist, the default will be returned.")
	public byte[] getIcon(@PathParam("iconKey") String iconKey) {
		IconModel iconModel = adminService.getIcon(iconKey);
		return iconModel.getData();
	}

	@GET
	@Produces("application/json")
	@Path("icon")
	@Operation(summary = "Get all icon keys.", description = "Returns a list of all the icon keys defined by the system.")
	public Set<String> getAllIconKeys() {
		Set<String> keys = adminService.getAllIconKeys();
		return keys;
	}

	@DELETE
	@Path("icon/{iconKey}")
	@Operation(summary = "Delete an icon.", description = "Deletes an icon associated with the given icon key.")
	public void deleteIcon(@PathParam("iconKey") String iconKey) {
		adminService.deleteIcon(iconKey);
	}

	@DELETE
	@Path("application/{id}")
	@Operation(summary = "Delete application definition.", description = "Deletes the given application definition from the database.  Will fail if the application defintion exists in an existing virtual machine template or virtual machine.")
	public void deleteApplicationDefinitions(@PathParam("id") String templateId) {
		adminService.deleteApplicationDefinition(templateId);
	}

	// JHU - Admin API - role create
	@POST
	@Produces("application/json")
	@Path("virtualMachine/template")
	@Operation(summary = "Create virtual machine template", description = "Creates a virtual machine template from the structure sent.  Some entries will be automatically filled by the server. (ID, created, updated fields)")
	public VirtualMachineTemplate createVmTemplate(VirtualMachineTemplate templateId) {
		return adminService.createVmTemplate(templateId);
	}

	@GET
	@Produces("application/json")
	@Path("virtualMachine/template/{id}")
	@Operation(summary = "Get virtual machine template.", description = "Returns the virtual machine template metadata for the given virtual machine template ID.")
	public VirtualMachineTemplate getVmTemplate(@PathParam("id") String templateId) {
		return adminService.getVmTemplate(templateId);
	}

	// JHU - Admin API - role list
	@GET
	@Produces("application/json")
	@Path("virtualMachine/template")
	@Operation(summary = "Get all virtual machine templates.", description = "Returns a list of all the virtual machine template metadata for the system.")
	public Iterable<VirtualMachineTemplate> getAllVmTemplates() {
		return adminService.getAllVmTemplates();
	}

	@PUT
	@Produces("application/json")
	@Path("virtualMachine/template/{id}")
	@Operation(summary = "Update virtual machine template,", description = "Updates a virtual machine template for the ID given in the path.")
	public VirtualMachineTemplate updateVmTemplate(@PathParam("id") String templateId, VirtualMachineTemplate vmt) {
		return adminService.updateVmTemplate(templateId, vmt);
	}

	@DELETE
	@Path("virtualMachine/template/{id}")
	@Operation(summary = "Delete virtual machine template", description = "Deletes the virtual machine template associated with the given ID.  The request will fail if there exist virtue templates associated with the virtual machine template.")
	public void deleteVmTemplate(@PathParam("id") String templateId) {
		adminService.deleteVmTemplate(templateId);
	}

	@PUT
	@Produces("application/json")
	@Path("virtualMachine/template/{id}/setStatus")
	@Operation(summary = "Enable/disable virtual machine template.", description = "Sets the virtual machine template with the given ID enabled flag to true or false based on the body text.")
	public VirtualMachineTemplate setVirtualMachineTemplateStatus(@PathParam("id") String templateId, String status) {
		boolean newStatus = Boolean.parseBoolean(status);
		VirtualMachineTemplate virtualMachineTemplate = adminService.setVirtualMachineTemplateStatus(templateId,
				newStatus);
		return virtualMachineTemplate;
	}

	@POST
	@Produces("application/json")
	@Path("virtue/template")
	@Operation(summary = "Create Virtue Template.", description = "Creates a new virtue template from the request body.")
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
	@Operation(summary = "Get all virtue templates.", description = "Returns a list of all the virtue templates in the system.")
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
	@Operation(summary = "Get virtue template", description = "Returns a single virtue template associated with the given ID.")
	public VirtueTemplate getVirtueTemplate(@PathParam("id") String templateId) {
		VirtueTemplate virtueTemplate = adminService.getVirtueTemplate(templateId);
		return virtueTemplate;
	}

	@PUT
	@Produces("application/json")
	@Path("virtue/template/{id}/setStatus")
	@Operation(summary = "Enable/disable virtue template.", description = "Sets the virtue template with the given ID enabled flag to true or false based on the body text.")
	public VirtueTemplate setVirtueTemplateStatus(@PathParam("id") String templateId, String newStatus) {
		boolean status = Boolean.parseBoolean(newStatus);
		VirtueTemplate virtueTemplate = adminService.setVirtueTemplateStatus(templateId, status);
		return virtueTemplate;
	}

	@PUT
	@Produces("application/json")
	@Path("virtue/template/{id}")
	@Operation(summary = "Update virtue template.", description = "Updates a virtue template associated with the ID in the path based on the request body.")
	public VirtueTemplate updateVirtueTemplate(@PathParam("id") String templateId, VirtueTemplate template) {
		VirtueTemplate virtueTemplate = adminService.updateVirtueTemplate(templateId, template);
		return virtueTemplate;
	}

	@DELETE
	@Path("virtue/template/{id}")
	@Operation(summary = "Delete virtue template.", description = "Delete virtue template associated with the given ID.  Request will fail if there exist users associated with the virtue template.")
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
	@Operation(summary = "Provision virtue", description = "Creates and starts the provisioning process of a virtue template into a virtue instance.")
	public VirtueInstance createVirtueFromTemplate(@PathParam("templateId") String templateId) {
		return desktopService.createVirtue(templateId);
	}

	// JHU - Admin API - user virtue list
	@GET
	@Produces("application/json")
	@Path("virtues")
	@Operation(summary = "Get all virtue instances", description = "Returns a list of virtue instances which are optionally filtered by user.")
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
	@Operation(summary = "Get virtue instance.", description = "Returns a single virtue instance associated with the given ID.")
	public VirtueInstance getActiveVirtue(@PathParam("id") String virtueId) {
		return adminService.getActiveVirtue(virtueId);
	}

	@GET
	@Path("deletevirtue/instance/{instanceId}")
	@Operation(summary = "Delete virtue instance.", description = "Deletes the virtue instance associated with the given ID.")
	public void deleteVirtue(@PathParam("instanceId") String instanceId) {
		adminService.deleteVirtue(instanceId);
	}

	@POST
	@Produces("application/json")
	@Path("user")
	@Operation(summary = "Create or update user.", description = "Creates or updates a user defined by the request body.  If a username with a matching username exists, that user will be updated, otherwise a new user will be created.")
	public VirtueUser createUpdateUser(VirtueUser newUser) {
		return adminService.createUpdateUser(newUser);
	}

	@PUT
	@Produces("application/json")
	@Path("user/{username}")
	@Operation(summary = "Update user.", description = "Updates a user based on the username included in the path.")
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
	@Operation(summary = "Get user.", description = "Returns user data associated with given username.")
	public VirtueUser getUser(@PathParam("username") String usernameToRetrieve) {
		VirtueUser returnedUser = adminService.getUser(usernameToRetrieve);
		return returnedUser;
	}

	// JHU - Admin API - user list
	@GET
	@Produces("application/json")
	@Path("user")
	@Operation(summary = "Get all users.", description = "Returns a list of all the users in the system.")
	public Iterable<VirtueUser> getAllUsers() {
		return adminService.getAllUsers();
	}

	@DELETE
	@Path("user/{username}")
	@Operation(summary = "Delete user.", description = "Deletes the user associated with the given username.")
	public void removeUser(@PathParam("username") String usernameToRemove) {
		adminService.removeUser(usernameToRemove);
	}

	@PUT
	@Path("user/{username}/setStatus")
	@Operation(summary = "Enable/disable user.", description = "For the user associated with given username, will enable or disable the user if the request body is true or false respectively.")
	public void setUserStatus(@PathParam("username") String username, String newStatus) {
		boolean status = Boolean.parseBoolean(newStatus);
		adminService.enableDisableUser(username, status);
	}

	// JHU - Admin API - user role authorize
	@POST // #TODO PUT? Is this idempotent? What in here should prevent duplicates?
	@Path("user/{username}/assign/{templateId}")
	@Operation(summary = "Assign virtue template to user.", description = "Will add the virtue template associated with the template ID to the user associated by the username.")
	public void assignTemplateToUser(@PathParam("username") String username,
			@PathParam("templateId") String templateId) {
		adminService.assignTemplateToUser(templateId, username);
	}

	// JHU - Admin API - user role unauthorize
	@POST // #TODO PUT
	@Path("user/{username}/revoke/{templateId}")
	@Operation(summary = "revoke virtue template from the user.", description = "Will remove the virtue template associated with the template ID from the user associated by the username.")
	public void revokeTemplateToUser(@PathParam("username") String username,
			@PathParam("templateId") String templateId) {
		adminService.revokeTemplateFromUser(templateId, username);
	}

	@GET
	@Path("user/active")
	@Produces("application/json")
	@Operation(summary = "Get all active users.", description = "Returns a list of users who are considered active.  Users are considered active if they have an active session.")
	public Iterable<VirtueUser> getActiveUsers() {
		return adminService.getActiveUsers();
	}

	// JHU - Admin API - user logout
	@GET
	@Path("user/{username}/logout")
	@Operation(summary = "Force user logout.", description = "Forces all sessions for the given user to be logged out.  This will include desktop application sessions as well as workbench sessions.  The user will then have to log back in.")
	public void logoutUser(@PathParam("username") String username) {
		adminService.logoutUser(username);
	}

	// JHU - Admin API - usertoken list
	@GET
	@Path("session")
	@Produces("application/json")
	@Operation(summary = "Get all sessions.", description = "Returns a list of all sessions currently active in the system.")
	public Map<String, List<String>> getAllSessions() {
		return adminService.getActiveSessions();
	}

	@GET
	@Path("session/{sessionId}")
	@Produces("application/json")
	@Operation(summary = "Get session.", description = "Returns metadata for a session associated with the given sessionId or token.")
	public VirtueSession getSession(@PathParam("sessionId") String sessionId) {
		return adminService.getActiveSession(sessionId);
	}

	@DELETE
	@Path("session/{sessionId}")
	@Operation(summary = "Invalidate session.", description = "Invalidates a single session based on the provided session ID.")
	public void invalidateSession(@PathParam("sessionId") String sessionId) {
		adminService.invalidateSession(sessionId);
	}

	@GET
	@Path("export")
	@Produces("application/json")
	@Operation(summary = "Export system to stream.", description = "Exports the entire system metadata for users, virtues templates, virtual machines templates, and applications (does not include disk images) to a internally defined format to be used for import.")
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

	@POST
	@Path("import")
	@Consumes("application/json")
	@Operation(summary = "Import system from stream", description = "Imports the system metadata from a provided stream file.  This format is the same as the Export system to stream.")
	public void importSystem(InputStream stream) {
		importExportService.importSystemDatabaseWithoutImages(stream);
	}

	@GET
	@Path("export/user")
	@Produces("application/zip")
	@Operation(summary = "Export all users to zip.", description = "Exports all users to a zip file.  This export traverses the user to include virtue templates, virtual machine templates, applications, icons and disk images to a zip file.  Each type is stored in its own directory.  Since this export contains images, it may take a long time to download.")
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
	@Operation(summary = "Export a single users to zip.", description = "Exports a single users to a zip file.  This export traverses the user to include virtue templates, virtual machine templates, applications, icons and disk images to a zip file.  Each type is stored in its own directory.  Since this export contains images, it may take a long time to download.")
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
	@Operation(summary = "Export virtue template to zip.", description = "Exports a virtue template to a zip file.  This export traverses the virtue template to include virtual machine templates, applications, icons and disk images to a zip file.  Each type is stored in its own directory.  Since this export contains images, it may take a long time to download.")
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
	@Operation(summary = "Export active system to zip.", description = "Exports all users to a zip file.  This export traverses the users to include virtue templates, virtual machine templates, applications, icons and disk images to a zip file.  Each type is stored in its own directory.  Since this export contains images, it may take a long time to download.")
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
	@Operation(summary = "Export all virtue templates to zip.", description = "Exports all virtue templates to a zip file.  This export traverses the virtue templates to include virtual machine templates, applications, icons and disk images to a zip file.  Each type is stored in its own directory.  Since this export contains images, it may take a long time to download.")
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
	@Operation(summary = "Export virtual machine template to zip.", description = "Exports virtual machine template to a zip file.  This export traverses the virtual machine template to include applications, icons and disk images to a zip file.  Each type is stored in its own directory.  Since this export contains images, it may take a long time to download.")
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
	@Consumes({ "application/zip", "application/octet-stream" })
	@Operation(summary = "Import zip", description = "Imports any users, virtue templates, virtual machine templates, applications, icons, and disk images provided by a zip.")
	public void importZip(InputStream stream,
			@QueryParam("waitForCompletion") @DefaultValue("true") boolean waitForCompletion) {
		importExportService.importZip(stream, waitForCompletion);
	}

	@GET
	@Path("import/{type}/{name}")
	@Produces("text/plain")
	@Operation(summary = "Import test objects", description = "Imports test objects from internal system into database.  When objects require other objects (I.E. virtue templates require virtual machine templates), the required objects will also be imported.  Objects can be of type: user, application, virtue, virtualmachines.  Users include admin, alice, bob.  Virtue templates include document_editor, external_internet_consumer, linux_corporate_email_user, linux_test_devel, power_user, router_admin, windows_corporate_email_user, windows_test_devel.  Virtual machine templates include external_internet_consumer, linux_all, linux_corporate_email_user, linux_power_user,	linux_router_admin,	linux_lo_impress, windows_all, windows_corporate_email_user, windows_document_editor, windows_power_user.  Applications include linux_calc, linux_chrome, linux_firefox, linux_lo_calc, linux_lo_draw, linux_lo_impress, linux_lo_writer, linux_terminal, linux_thunderbird, windows_chrome, windows_cmd, windows_edge, windows_excel, windows_outlook, windows_powershell, windows_skype, windows_word")
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
	@Operation(summary = "Import all test objects", description = "Imports all test objects in the system.")
	public String importAll() {
		int items = importExportService.importAll();
		return "imported " + items + " items.";
	}

	@GET
	@Path("sensing")
	@Produces("application/json")
	@Operation(hidden = true)
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
	@Operation(summary = "Get all virtual machines.", description = "Returns a list of all metadata for active virtual machines.")
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
	@Operation(summary = "Get virtual machine.", description = "Returns the metadata for a single virtual machine.")
	public VirtualMachine getVm(@PathParam("id") String id) {
		try {
			return adminService.getVm(id);
		} catch (Exception e) {
			throw WebServiceUtil.createWebserviceException(e);
		}
	}

	@GET
	@Path("vm/reboot/{id}")
	@Operation(summary = "Reboot virtual machine.", description = "Reboots a single virtual machine associated with the given ID.")
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
	@Operation(summary = "Get all clipboard permissions.", description = "Returns a list of all the available clipboard permissions.  Permissions contain an heirarchy where if no permission is explicitly set, a higher permission will take its place.  The heirarchy is system default, source default, and then source/destination combination.  All permissions will be returned by default.  If the query parameter 'raw' is true, then only permissions that are explicitly set are returned.")
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
	@Operation(summary = "Set clipboard permission.", description = "Sets the clipboard permission from the request body for the given source ID and destination ID.  Source and destination IDs are virtue template IDs.")
	public String setPermission(@PathParam("sourceId") String sourceId, @PathParam("destId") String destId,
			@RequestBody(description = "Clipboard permission option", required = true, content = @Content(schema = @Schema(implementation = ClipboardPermissionOption.class))) String optionStr) {
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
	@Operation(summary = "Get clipboard permission.", description = "Returns the clipboard permission for the given source ID and destination ID.  Source and destination IDs are virtue template IDs.")
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
	@Operation(summary = "Get default permission for source.", description = "Returns the clipboard permission for the given source ID.  This permission will be used if a specific source and destination combination for this source is not explicitly defined.  Source and destination IDs are virtue template IDs.")
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
	@Operation(summary = "Delete clipboard permission.", description = "Clears the clipboard permission for the given source ID and destination ID.  Source and destination IDs are virtue template IDs.")
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
	@Operation(summary = "Set system default clipboard permission.", description = "Sets the clipboard permission default for the system.")
	public void setServiceDefaultPermission(
			@PathParam("option") @Parameter(description = "Clipboard permission option", required = true, content = @Content(schema = @Schema(implementation = ClipboardPermissionOption.class))) String optionStr) {
		ClipboardPermissionOption option = ClipboardPermissionOption.valueOf(optionStr);
		permissionService.setDefaultClipboardPermission(option);
	}

	@GET
	@Path("storage")
	@Produces("application/json")
	@Operation(summary = "Get all persistent storage.", description = "Returns a list of all persistent storage entries in the system.")
	public Iterable<VirtuePersistentStorage> getAllStorage() {
		return adminService.getAllPersistentStorage();
	}

	@GET
	@Path("storage/{user}/")
	@Produces("application/json")
	@Operation(summary = "Get persistent storage entries for a user.", description = "Returns a list of all persistent storage entries for the given user.")
	public Iterable<VirtuePersistentStorage> getPersistentStorageForUser(@PathParam("user") String username) {
		return adminService.getPersistentStorageForUser(username);
	}

	@GET
	@Path("storage/{user}/{virtueTemplateId}")
	@Produces("application/json")
	@Operation(summary = "Get persistent storage entries for a user and virtue template.", description = "Returns a persistent storage entry for the given user and virtue template ID.")
	public VirtuePersistentStorage getPersistentStorage(@PathParam("user") String username,
			@PathParam("virtueTemplateId") String virtueTemplateId) {
		return adminService.getPersistentStorage(username, virtueTemplateId);
	}

	@DELETE
	@Path("storage/{user}/{virtueTemplateId}")
	@Operation(summary = "Delete persistent storage entries for a user and virtue template.", description = "Deletes a persistent storage entry for the given user and virtue template ID.")
	public void deletePersistentStorage(@PathParam("user") String username,
			@PathParam("virtueTemplateId") String virtueTemplateId) {
		adminService.deletePersistentStorage(username, virtueTemplateId);
	}

	@GET
	@Path("securityGroup")
	@Produces("application/json")
	@Operation(summary = "Get all network security group permissions.", description = "Returns a list of all network security group permissions in the system.")
	public Map<String, Collection<SecurityGroupPermission>> getAllSecurityGroupsAndPermissions() {
		return adminService.getAllSecurityGroups();
	}

	@GET
	@Path("securityGroup/id/{groupId}")
	@Produces("application/json")
	@Operation(summary = "Get network security group permissions for a single security group.", description = "Returns a list of all network security group permissions for a security group ID.")
	public Collection<SecurityGroupPermission> getPermissionsByGroupId(@PathParam("groupId") String groupId) {
		return adminService.getSecurityGroupPermissions(groupId);
	}

	@GET
	@Path("securityGroup/template/{templateId}")
	@Produces("application/json")
	@Operation(summary = "Get network security group permissions for a virtue template.", description = "Returns a list of all network security group permissions for a virtue template ID.")
	public Collection<SecurityGroupPermission> getGroupIdByTemplate(@PathParam("templateId") String templateId) {
		return adminService.getSecurityGroupPermissionsByTemplate(templateId);
	}

	@POST // #TODO PUT
	@Path("securityGroup/template/{templateId}/revoke")
	@Operation(summary = "Remove single network security group permission from a virtue template.", description = "Removes a single security group permission from a virtue template's security group.")
	public void revokePermissionForTemplate(@PathParam("templateId") String templateId, SecurityGroupPermission sgp) {
		adminService.revokeSecurityGroupsByKey(templateId, sgp);
	}

	@POST // #TODO PUT?
	@Path("securityGroup/template/{templateId}/authorize")
	@Operation(summary = "Adds single network security group permission for a virtue template.", description = "Adds a single security group permission to a virtue template's security group.")
	public void authorizePermissionFromTemplate(@PathParam("templateId") String templateId,
			SecurityGroupPermission sgp) {
		adminService.authorizeSecurityGroupsByKey(templateId, sgp);
	}

	@DELETE
	@Path("securityGroup/template/{templateId}")
	@Operation(summary = "Deletes the network security group for a virtue template.", description = "Deletes the security group from a virtue template's.")
	public void deleteByTemplateId(@PathParam("templateId") String templateId) {
		String secGroupId = adminService.securityGroupIdByTemplateId(templateId);
		adminService.deleteSecurityGroup(secGroupId);
	}

	@DELETE
	@Path("securityGroup/id/{groupId}")
	@Operation(summary = "Deletes the network security group by security group ID.", description = "Deletes the security group by security group ID.")
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
	@Path("printer")
	@Operation(hidden = true)
	public Printer createPrinter(Printer printer) {
		return adminService.createPrinter(printer);
	}

	@PUT
	@Produces("application/json")
	@Path("printer/{printerId}")
	@Operation(hidden = true)
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
	@Operation(hidden = true)
	public Printer getPrinter(@PathParam("printerId") String printerId) {
		Printer returnedPrinter = adminService.getPrinter(printerId);
		return returnedPrinter;
	}

	// JHU - Admin API - user list
	@GET
	@Produces("application/json")
	@Path("printer")
	@Operation(hidden = true)
	public Iterable<Printer> getAllPrinters() {
		return adminService.getAllPrinters();
	}

	@DELETE
	@Path("printer/{printerId}")
	@Operation(hidden = true)
	public void deletePrinter(@PathParam("printerId") String printerId) {
		adminService.deletePrinter(printerId);
	}

	@PUT
	@Path("printer/{printerId}/setStatus")
	@Operation(hidden = true)
	public void setPrinterStatus(@PathParam("printerId") String printerId, String enableString) {
		boolean enable = Boolean.parseBoolean(enableString);
		adminService.setPrinterStatus(printerId, enable);
	}

	/********************/

	@POST
	@Produces("application/json")
	@Path("fileSystem")
	@Operation(summary = "Creates metadata for a shared file system.", description = "Creates the metadata for a shared file system (I.E. Samba share).")
	public FileSystem createFileSystem(FileSystem fileSystem) {
		return adminService.createFileSystem(fileSystem);
	}

	@PUT
	@Produces("application/json")
	@Path("fileSystem/{fileSystemId}")
	@Operation(summary = "Updates metadata for a shared file system.", description = "Updates the metadata for a shared file system (I.E. Samba share).")
	public FileSystem updateFileSystem(@PathParam("fileSystemId") String fileSystemId, FileSystem fileSystem) {
		if (!fileSystem.getId().equals(fileSystemId)) {
			throw new SaviorException(SaviorErrorCode.ID_MISMATCH,
					"Given fileSystem doesn't match input id. FileSystem ID=" + fileSystemId + ". FileSystem="
							+ fileSystem);
		}
		return adminService.updateFileSystem(fileSystemId, fileSystem);
	}

	@GET
	@Produces("application/json")
	@Path("fileSystem/{fileSystemId}")
	@Operation(summary = "Get metadata for a shared file system.", description = "Returns the metadata for a shared file system (I.E. Samba share).")
	public FileSystem getFileSystem(@PathParam("fileSystemId") String fileSystemId) {
		FileSystem returnedFileSystem = adminService.getFileSystem(fileSystemId);
		return returnedFileSystem;
	}

	@GET
	@Produces("application/json")
	@Path("fileSystem")
	@Operation(summary = "Get all shared file systems.", description = "Returns a list of all the metadata for a shared file systems(I.E. Samba share) on the system.")
	public Iterable<FileSystem> getAllFileSystems() {
		return adminService.getAllFileSystems();
	}

	@DELETE
	@Path("fileSystem/{fileSystemId}")
	@Operation(summary = "Delete metadata for a shared file system.", description = "Deletes the metadat for a shared file system (I.E. Samba share).")
	public void deleteFileSystem(@PathParam("fileSystemId") String fileSystemId) {
		adminService.deleteFileSystem(fileSystemId);
	}

	@PUT
	@Path("fileSystem/{fileSystemId}/setStatus")
	@Operation(summary = "Enable/disable a shared file system.", description = "Enables/disables a shared file system (I.E. Samba share).")
	public void setFileSystemStatus(@PathParam("fileSystemId") String fileSystemId,
			@RequestBody(description = "Enabled boolean", required = true, content = @Content(schema = @Schema(implementation = Boolean.class, example = "true"))) String enableString) {
		boolean enable = Boolean.parseBoolean(enableString);
		adminService.setFileSystemStatus(fileSystemId, enable);
	}

	@GET
	@Path("sync")
	@Operation(hidden = true)
	public void sync() {
		adminService.sync();
	}

}
