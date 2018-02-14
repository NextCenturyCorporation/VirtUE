package com.ncc.savior.virtueadmin.rest;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.springframework.beans.factory.annotation.Autowired;

import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.User;
import com.ncc.savior.virtueadmin.model.VirtualMachineTemplate;
import com.ncc.savior.virtueadmin.model.VirtueInstance;
import com.ncc.savior.virtueadmin.model.VirtueTemplate;
import com.ncc.savior.virtueadmin.security.UserService;
import com.ncc.savior.virtueadmin.service.AdminService;
import com.ncc.savior.virtueadmin.service.DesktopVirtueService;
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

	public AdminResource() {

	}

	@POST
	@Produces("application/json")
	@Path("application")
	public ApplicationDefinition createNewApplicationDefinition(ApplicationDefinition appDef) {
		try {
			User user = getUserFromSecurity();
			return adminService.createNewApplicationDefinition(user, appDef);
		} catch (RuntimeException e) {
			// TODO fix createWebserviceException
			// Probably need to create our own exception
			// Needs to create ExceptionMapper for jersey.
			throw WebServiceUtil.createWebserviceException(e);
		}
	}

	@GET
	@Produces("application/json")
	@Path("application")
	public Iterable<ApplicationDefinition> getAllApplicationDefinitions() {
		try {
			User user = getUserFromSecurity();
			return adminService.getAllApplicationTemplates(user);
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
			User user = getUserFromSecurity();
			return adminService.getApplicationDefinition(user, templateId);
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
			User user = getUserFromSecurity();
			return adminService.updateApplicationDefinitions(user, templateId, appDef);
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
			User user = getUserFromSecurity();
			adminService.deleteApplicationDefinition(user, templateId);
		} catch (RuntimeException e) {
			// TODO fix createWebserviceException
			// Probably need to create our own exception
			// Needs to create ExceptionMapper for jersey.
			throw WebServiceUtil.createWebserviceException(e);
		}
	}

	@POST
	@Produces("application/json")
	@Path("virtualMachine/template/")
	public VirtualMachineTemplate createVmTemplate(VirtualMachineTemplate templateId) {
		try {
			User user = getUserFromSecurity();
			return adminService.createVmTemplate(user, templateId);
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
			User user = getUserFromSecurity();
			return adminService.getVmTemplate(user, templateId);
		} catch (RuntimeException e) {
			// TODO fix createWebserviceException
			// Probably need to create our own exception
			// Needs to create ExceptionMapper for jersey.
			throw WebServiceUtil.createWebserviceException(e);
		}
	}

	@GET
	@Produces("application/json")
	@Path("virtualMachine/template")
	public Iterable<VirtualMachineTemplate> getAllVmTemplates() {
		try {
			User user = getUserFromSecurity();
			return adminService.getAllVmTemplates(user);
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
			User user = getUserFromSecurity();
			return adminService.updateVmTemplate(user, templateId, vmt);
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
			User user = getUserFromSecurity();
			adminService.deleteVmTemplate(user, templateId);
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
			User user = getUserFromSecurity();
			VirtueTemplate virtueTemplate = adminService.createNewVirtueTemplate(user, template);
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
	public Iterable<VirtueTemplate> getAllVirtueTemplates() {
		try {
			User user = getUserFromSecurity();
			Iterable<VirtueTemplate> virtueTemplates = adminService.getAllVirtueTemplates(user);
			return virtueTemplates;
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
			User user = getUserFromSecurity();
			VirtueTemplate virtueTemplate = adminService.getVirtueTemplate(user, templateId);
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
			User user = getUserFromSecurity();
			VirtueTemplate virtueTemplate = adminService.updateVirtueTemplate(user, templateId, template);
			return virtueTemplate;
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
			User user = getUserFromSecurity();
			adminService.deleteVirtue(user, instanceId);
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
			User user = getUserFromSecurity();
			return desktopService.createVirtue(user, templateId);
		} catch (Exception e) {
			// TODO fix createWebserviceException
			// Probably need to create our own exception
			// Needs to create ExceptionMapper for jersey.
			throw WebServiceUtil.createWebserviceException(e);
		}
	}

	@GET
	@Produces("application/json")
	@Path("virtues")
	public Iterable<VirtueInstance> getAllActiveVirtues() {
		try {
			User user = getUserFromSecurity();
			return adminService.getAllActiveVirtues(user);
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
			User user = getUserFromSecurity();
			return adminService.getActiveVirtue(user, virtueId);
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
	public User createUpdateUser(User newUser) {
		try {
			User user = getUserFromSecurity();
			return adminService.createUpdateUser(user, newUser);
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
	public User updateUser(@PathParam("username") String username, User newUser) {
		try {
			User user = getUserFromSecurity();
			if (!newUser.getUsername().equals(username)) {
				throw new SaviorException(SaviorException.UNKNOWN_ERROR,
						"Given user doesn't match username in path.  Username=" + username + ". NewUser=" + newUser);
			}
			return adminService.createUpdateUser(user, newUser);
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
	public User getUser(@PathParam("username") String usernameToRetrieve) {
		try {
			User user = getUserFromSecurity();
			User returnedUser = user= adminService.getUser(user, usernameToRetrieve);
			return returnedUser;
		} catch (RuntimeException e) {
			// TODO fix createWebserviceException
			// Probably need to create our own exception
			// Needs to create ExceptionMapper for jersey.
			throw WebServiceUtil.createWebserviceException(e);
		}
	}
	
	@GET
	@Produces("application/json")
	@Path("user")
	public Iterable<User> getAllUsers() {
		try {
			User user = getUserFromSecurity();
			return adminService.getAllUsers(user);
		} catch (RuntimeException e) {
			// TODO fix createWebserviceException
			// Probably need to create our own exception
			// Needs to create ExceptionMapper for jersey.
			throw WebServiceUtil.createWebserviceException(e);
		}
	}

	@DELETE
	@Produces("application/json")
	@Path("user/{username}")
	public void removeUser(@PathParam("username") String usernameToRemove) {
		try {
			User user = getUserFromSecurity();
			adminService.removeUser(user, usernameToRemove);
		} catch (RuntimeException e) {
			// TODO fix createWebserviceException
			// Probably need to create our own exception
			// Needs to create ExceptionMapper for jersey.
			throw WebServiceUtil.createWebserviceException(e);
		}
	}

	private User getUserFromSecurity() {
		User user = UserService.getCurrentUser();
		return user;
	}
}
