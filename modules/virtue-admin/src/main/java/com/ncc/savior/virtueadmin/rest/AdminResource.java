package com.ncc.savior.virtueadmin.rest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.springframework.beans.factory.annotation.Autowired;

import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.VirtualMachineTemplate;
import com.ncc.savior.virtueadmin.model.VirtueInstance;
import com.ncc.savior.virtueadmin.model.VirtueSession;
import com.ncc.savior.virtueadmin.model.VirtueTemplate;
import com.ncc.savior.virtueadmin.model.VirtueUser;
import com.ncc.savior.virtueadmin.service.AdminService;
import com.ncc.savior.virtueadmin.service.DesktopVirtueService;
import com.ncc.savior.virtueadmin.service.ImportExportService;
import com.ncc.savior.virtueadmin.util.SaviorException;
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
		try {
			return adminService.createNewApplicationDefinition(appDef);
		} catch (RuntimeException e) {
			// TODO fix createWebserviceException
			// Probably need to create our own exception
			// Needs to create ExceptionMapper for jersey.
			throw WebServiceUtil.createWebserviceException(e);
		}
	}

	// JHU - Admin API - application list
	@GET
	@Produces("application/json")
	@Path("application")
	public Iterable<ApplicationDefinition> getAllApplicationDefinitions() {
		try {
			return adminService.getAllApplicationTemplates();
		} catch (RuntimeException e) {
			// TODO fix createWebserviceException
			// Probably need to create our own exception
			// Needs to create ExceptionMapper for jersey.
			throw WebServiceUtil.createWebserviceException(e);
		}
	}

	@GET
	@Produces("application/json")
	@Path("application/{id}")
	public ApplicationDefinition getApplicationDefinition(@PathParam("id") String templateId) {
		try {
			return adminService.getApplicationDefinition(templateId);
		} catch (RuntimeException e) {
			// TODO fix createWebserviceException
			// Probably need to create our own exception
			// Needs to create ExceptionMapper for jersey.
			throw WebServiceUtil.createWebserviceException(e);
		}
	}

	@PUT
	@Produces("application/json")
	@Path("application")
	public ApplicationDefinition updateApplicationDefinitions(@PathParam("id") String templateId,
			ApplicationDefinition appDef) {
		try {
			return adminService.updateApplicationDefinitions(templateId, appDef);
		} catch (RuntimeException e) {
			// TODO fix createWebserviceException
			// Probably need to create our own exception
			// Needs to create ExceptionMapper for jersey.
			throw WebServiceUtil.createWebserviceException(e);
		}
	}

	@DELETE
	@Path("application/{id}")
	public void deleteApplicationDefinitions(@PathParam("id") String templateId) {
		try {
			adminService.deleteApplicationDefinition(templateId);
		} catch (RuntimeException e) {
			// TODO fix createWebserviceException
			// Probably need to create our own exception
			// Needs to create ExceptionMapper for jersey.
			throw WebServiceUtil.createWebserviceException(e);
		}
	}

	// JHU - Admin API - role create
	@POST
	@Produces("application/json")
	@Path("virtualMachine/template/")
	public VirtualMachineTemplate createVmTemplate(VirtualMachineTemplate templateId) {
		try {
			return adminService.createVmTemplate(templateId);
		} catch (RuntimeException e) {
			// TODO fix createWebserviceException
			// Probably need to create our own exception
			// Needs to create ExceptionMapper for jersey.
			throw WebServiceUtil.createWebserviceException(e);
		}
	}

	@GET
	@Produces("application/json")
	@Path("virtualMachine/template/{id}")
	public VirtualMachineTemplate getVmTemplate(@PathParam("id") String templateId) {
		try {
			return adminService.getVmTemplate(templateId);
		} catch (RuntimeException e) {
			// TODO fix createWebserviceException
			// Probably need to create our own exception
			// Needs to create ExceptionMapper for jersey.
			throw WebServiceUtil.createWebserviceException(e);
		}
	}

	// JHU - Admin API - role list
	@GET
	@Produces("application/json")
	@Path("virtualMachine/template")
	public Iterable<VirtualMachineTemplate> getAllVmTemplates() {
		try {
			return adminService.getAllVmTemplates();
		} catch (RuntimeException e) {
			// TODO fix createWebserviceException
			// Probably need to create our own exception
			// Needs to create ExceptionMapper for jersey.
			throw WebServiceUtil.createWebserviceException(e);
		}
	}

	@PUT
	@Produces("application/json")
	@Path("virtualMachine/template/{id}")
	public VirtualMachineTemplate updateVmTemplate(@PathParam("id") String templateId, VirtualMachineTemplate vmt) {
		try {
			return adminService.updateVmTemplate(templateId, vmt);
		} catch (RuntimeException e) {
			// TODO fix createWebserviceException
			// Probably need to create our own exception
			// Needs to create ExceptionMapper for jersey.
			throw WebServiceUtil.createWebserviceException(e);
		}
	}

	@DELETE
	@Path("virtualMachine/template/{id}")
	public void deleteVmTemplate(@PathParam("id") String templateId) {
		try {
			adminService.deleteVmTemplate(templateId);
		} catch (RuntimeException e) {
			// TODO fix createWebserviceException
			// Probably need to create our own exception
			// Needs to create ExceptionMapper for jersey.
			throw WebServiceUtil.createWebserviceException(e);
		}
	}

	@POST
	@Produces("application/json")
	@Path("virtue/template")
	public VirtueTemplate createNewVirtueTemplate(VirtueTemplate template) {
		try {
			VirtueTemplate virtueTemplate = adminService.createNewVirtueTemplate(template);
			return virtueTemplate;
		} catch (RuntimeException e) {
			// TODO fix createWebserviceException
			// Probably need to create our own exception
			// Needs to create ExceptionMapper for jersey.
			throw WebServiceUtil.createWebserviceException(e);
		}
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
		try {
			if (user == null || user.trim().equals("")) {
				Iterable<VirtueTemplate> virtueTemplates = adminService.getAllVirtueTemplates();
				return virtueTemplates;
			} else {
				Iterable<VirtueTemplate> virtueTemplates = adminService.getVirtueTemplatesForUser(user);
				return virtueTemplates;
			}
		} catch (RuntimeException e) {
			// TODO fix createWebserviceException
			// Probably need to create our own exception
			// Needs to create ExceptionMapper for jersey.
			throw WebServiceUtil.createWebserviceException(e);
		}
	}

	@GET
	@Produces("application/json")
	@Path("virtue/template/{id}")
	public VirtueTemplate getVirtueTemplate(@PathParam("id") String templateId) {
		try {
			VirtueTemplate virtueTemplate = adminService.getVirtueTemplate(templateId);
			return virtueTemplate;
		} catch (RuntimeException e) {
			// TODO fix createWebserviceException
			// Probably need to create our own exception
			// Needs to create ExceptionMapper for jersey.
			throw WebServiceUtil.createWebserviceException(e);
		}
	}

	@POST
	@Produces("application/json")
	@Path("virtue/template/{id}")
	public VirtueTemplate updateVirtueTemplate(@PathParam("id") String templateId, VirtueTemplate template) {
		try {
			VirtueTemplate virtueTemplate = adminService.updateVirtueTemplate(templateId, template);
			return virtueTemplate;
		} catch (RuntimeException e) {
			// TODO fix createWebserviceException
			// Probably need to create our own exception
			// Needs to create ExceptionMapper for jersey.
			throw WebServiceUtil.createWebserviceException(e);
		}
	}

	@DELETE
	@Produces("application/json")
	@Path("virtue/template/{id}")
	public void deleteVirtueTemplate(@PathParam("id") String templateId) {
		try {
			adminService.deleteVirtueTemplate(templateId);
		} catch (RuntimeException e) {
			// TODO fix createWebserviceException
			// Probably need to create our own exception
			// Needs to create ExceptionMapper for jersey.
			throw WebServiceUtil.createWebserviceException(e);
		}
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
		try {
			return desktopService.createVirtue(templateId);
		} catch (Exception e) {
			// TODO fix createWebserviceException
			// Probably need to create our own exception
			// Needs to create ExceptionMapper for jersey.
			throw WebServiceUtil.createWebserviceException(e);
		}
	}

	// JHU - Admin API - user virtue list
	@GET
	@Produces("application/json")
	@Path("virtues")
	public Iterable<VirtueInstance> getAllActiveVirtues(@QueryParam("user") String username) {
		try {
			if (username == null || username.trim().equals("")) {
				return adminService.getAllActiveVirtues();
			} else {
				return adminService.getAllActiveVirtuesForUser(username);
			}
		} catch (RuntimeException e) {
			// TODO fix createWebserviceException
			// Probably need to create our own exception
			// Needs to create ExceptionMapper for jersey.
			throw WebServiceUtil.createWebserviceException(e);
		}
	}

	@GET
	@Produces("application/json")
	@Path("virtues/{id}")
	public VirtueInstance getActiveVirtue(@PathParam("id") String virtueId) {
		try {
			return adminService.getActiveVirtue(virtueId);
		} catch (RuntimeException e) {
			// TODO fix createWebserviceException
			// Probably need to create our own exception
			// Needs to create ExceptionMapper for jersey.
			throw WebServiceUtil.createWebserviceException(e);
		}
	}

	@GET
	@Path("deletevirtue/instance/{instanceId}")
	public void deleteVirtue(@PathParam("instanceId") String instanceId) {
		try {
			adminService.deleteVirtue(instanceId);
		} catch (RuntimeException e) {
			// TODO fix createWebserviceException
			// Probably need to create our own exception
			// Needs to create ExceptionMapper for jersey.
			throw WebServiceUtil.createWebserviceException(e);
		}
	}

	@POST
	@Produces("application/json")
	@Path("user/")
	public VirtueUser createUpdateUser(VirtueUser newUser) {
		try {
			return adminService.createUpdateUser(newUser);
		} catch (RuntimeException e) {
			// TODO fix createWebserviceException
			// Probably need to create our own exception
			// Needs to create ExceptionMapper for jersey.
			throw WebServiceUtil.createWebserviceException(e);
		}
	}

	@PUT
	@Produces("application/json")
	@Path("user/{username}")
	public VirtueUser updateUser(@PathParam("username") String username, VirtueUser newUser) {
		try {
			if (!newUser.getUsername().equals(username)) {
				throw new SaviorException(SaviorException.UNKNOWN_ERROR,
						"Given user doesn't match username in path.  Username=" + username + ". NewUser=" + newUser);
			}
			return adminService.createUpdateUser(newUser);
		} catch (RuntimeException e) {
			// TODO fix createWebserviceException
			// Probably need to create our own exception
			// Needs to create ExceptionMapper for jersey.
			throw WebServiceUtil.createWebserviceException(e);
		}
	}

	@GET
	@Produces("application/json")
	@Path("user/{username}")
	public VirtueUser getUser(@PathParam("username") String usernameToRetrieve) {
		try {
			VirtueUser returnedUser = adminService.getUser(usernameToRetrieve);
			return returnedUser;
		} catch (RuntimeException e) {
			// TODO fix createWebserviceException
			// Probably need to create our own exception
			// Needs to create ExceptionMapper for jersey.
			throw WebServiceUtil.createWebserviceException(e);
		}
	}

	// JHU - Admin API - user list
	@GET
	@Produces("application/json")
	@Path("user")
	public Iterable<VirtueUser> getAllUsers() {
		try {
			return adminService.getAllUsers();
		} catch (RuntimeException e) {
			// TODO fix createWebserviceException
			// Probably need to create our own exception
			// Needs to create ExceptionMapper for jersey.
			throw WebServiceUtil.createWebserviceException(e);
		}
	}

	// JHU - Admin API - user get
	@DELETE
	@Produces("application/json")
	@Path("user/{username}")
	public void removeUser(@PathParam("username") String usernameToRemove) {
		try {
			adminService.removeUser(usernameToRemove);
		} catch (RuntimeException e) {
			// TODO fix createWebserviceException
			// Probably need to create our own exception
			// Needs to create ExceptionMapper for jersey.
			throw WebServiceUtil.createWebserviceException(e);
		}
	}

	// JHU - Admin API - user role authorize
	@POST
	@Produces("application/json")
	@Path("user/{username}/assign/{templateId}")
	public void assignTemplateToUser(@PathParam("username") String username,
			@PathParam("templateId") String templateId) {
		try {
			adminService.assignTemplateToUser(templateId, username);
		} catch (RuntimeException e) {
			// TODO fix createWebserviceException
			// Probably need to create our own exception
			// Needs to create ExceptionMapper for jersey.
			throw WebServiceUtil.createWebserviceException(e);
		}
	}

	// JHU - Admin API - user role unauthorize
	@POST
	@Produces("application/json")
	@Path("user/{username}/revoke/{templateId}")
	public void revokeTemplateToUser(@PathParam("username") String username,
			@PathParam("templateId") String templateId) {
		try {
			adminService.revokeTemplateFromUser(templateId, username);
		} catch (RuntimeException e) {
			// TODO fix createWebserviceException
			// Probably need to create our own exception
			// Needs to create ExceptionMapper for jersey.
			throw WebServiceUtil.createWebserviceException(e);
		}
	}

	@GET
	@Path("user/active")
	@Produces("application/json")
	public Iterable<VirtueUser> getActiveUsers() {
		try {
			return adminService.getActiveUsers();
		} catch (RuntimeException e) {
			// TODO fix createWebserviceException
			// Probably need to create our own exception
			// Needs to create ExceptionMapper for jersey.
			throw WebServiceUtil.createWebserviceException(e);
		}
	}

	// JHU - Admin API - user logout
	@GET
	@Path("user/{username}/logout")
	@Produces("application/json")
	public void logoutUser(@PathParam("username") String username) {
		try {
			adminService.logoutUser(username);
		} catch (RuntimeException e) {
			// TODO fix createWebserviceException
			// Probably need to create our own exception
			// Needs to create ExceptionMapper for jersey.
			throw WebServiceUtil.createWebserviceException(e);
		}
	}

	// JHU - Admin API - usertoken list
	@GET
	@Path("session")
	@Produces("application/json")
	public Map<String, List<String>> getAllSessions() {
		try {
			return adminService.getActiveSessions();
		} catch (RuntimeException e) {
			// TODO fix createWebserviceException
			// Probably need to create our own exception
			// Needs to create ExceptionMapper for jersey.
			throw WebServiceUtil.createWebserviceException(e);
		}
	}

	@GET
	@Path("session/{sessionId}")
	@Produces("application/json")
	public VirtueSession getSession(@PathParam("sessionId") String sessionId) {
		try {
			return adminService.getActiveSession(sessionId);
		} catch (RuntimeException e) {
			// TODO fix createWebserviceException
			// Probably need to create our own exception
			// Needs to create ExceptionMapper for jersey.
			throw WebServiceUtil.createWebserviceException(e);
		}
	}

	@DELETE
	@Path("session/{sessionId}")
	@Produces("application/json")
	public void invalidateSession(@PathParam("sessionId") String sessionId) {
		try {
			adminService.invalidateSession(sessionId);
		} catch (RuntimeException e) {
			// TODO fix createWebserviceException
			// Probably need to create our own exception
			// Needs to create ExceptionMapper for jersey.
			throw WebServiceUtil.createWebserviceException(e);
		}
	}

	@GET
	@Path("export")
	@Produces("application/json")
	public Response exportSystem() {
		try {
			StreamingOutput stream = new StreamingOutput() {
				@Override
				public void write(OutputStream os) throws IOException, WebApplicationException {
					importExportService.exportSystem(os);
					os.flush();
				}
			};
			return Response.ok(stream).build();
		} catch (RuntimeException e) {
			// TODO fix createWebserviceException
			// Probably need to create our own exception
			// Needs to create ExceptionMapper for jersey.
			throw WebServiceUtil.createWebserviceException(e);
		}
	}

	@POST
	@Path("import")
	@Produces("application/json")
	public void importSystem(InputStream stream) {
		try {
			importExportService.importSystem(stream);
		} catch (RuntimeException e) {
			// TODO fix createWebserviceException
			// Probably need to create our own exception
			// Needs to create ExceptionMapper for jersey.
			throw WebServiceUtil.createWebserviceException(e);
		} catch (IOException e) {
			throw WebServiceUtil.createWebserviceException(e);
		}
	}
}
