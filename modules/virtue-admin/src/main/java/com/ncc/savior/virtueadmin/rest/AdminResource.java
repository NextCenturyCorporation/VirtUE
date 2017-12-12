package com.ncc.savior.virtueadmin.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.springframework.beans.factory.annotation.Autowired;

import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.VirtualMachineTemplate;
import com.ncc.savior.virtueadmin.model.VirtueTemplate;
import com.ncc.savior.virtueadmin.service.AdminService;
import com.ncc.savior.virtueadmin.util.WebServiceUtil;

/**
 * Rest resource that handles endpoints specifically for an administrator
 * 
 */

@Path("/admin")
public class AdminResource {

	@Autowired
	private AdminService adminService;

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
			Iterable<VirtueTemplate> virtueTemplates = adminService.getAllVirtueTemplates();
			return virtueTemplates;
		} catch (RuntimeException e) {
			// TODO fix createWebserviceException
			// Probably need to create our own exception
			// Needs to create ExceptionMapper for jersey.
			throw WebServiceUtil.createWebserviceException(e);
		}
	}

	// @GET
	// @Produces("application/json")
	// @Path("virtue/template/{virtueTemplateId}")
	// public VirtueTemplate getVirtueTemplate(@PathParam("virtueTemplateId") String
	// virtueTemplateId) {
	// try {
	// VirtueTemplate virtueTemplate =
	// adminService.getVirtueTemplate(virtueTemplateId);
	// return virtueTemplate;
	// } catch (RuntimeException e) {
	// // TODO fix createWebserviceException
	// // Probably need to create our own exception
	// // Needs to create ExceptionMapper for jersey.
	// throw WebServiceUtil.createWebserviceException(e);
	// }
	// }

	/**
	 * Get all {@link VirtualMachineTemplate}s in the system
	 * 
	 * @return
	 */
	@GET
	@Produces("application/json")
	@Path("virtualMachine/template")
	public Iterable<VirtueTemplate> getAllVmTemplates() {
		try {
			return adminService.getAllVmTemplates();
		} catch (RuntimeException e) {
			// TODO fix createWebserviceException
			// Probably need to create our own exception
			// Needs to create ExceptionMapper for jersey.
			throw WebServiceUtil.createWebserviceException(e);
		}
	}

	// @GET
	// @Produces("application/json")
	// @Path("virtualMachine/template/{vmTemplateId}")
	// public VirtualMachineTemplate getAllVmTemplates(@PathParam("vmTemplateId")
	// String vmTemplateId) {
	// try {
	// return adminService.getVmTemplate(vmTemplateId);
	// } catch (RuntimeException e) {
	// // TODO fix createWebserviceException
	// // Probably need to create our own exception
	// // Needs to create ExceptionMapper for jersey.
	// throw WebServiceUtil.createWebserviceException(e);
	// }
	// }

	/**
	 * Get all application definitions
	 * 
	 * @return
	 */
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

	// @GET
	// @Produces("application/json")
	// @Path("application")
	// public ApplicationDefinition
	// getApplicationDefinition(@PathParam("applicationId") String applicationId) {
	// try {
	// return adminService.getApplicationTemplate(applicationId);
	// } catch (RuntimeException e) {
	// // TODO fix createWebserviceException
	// // Probably need to create our own exception
	// // Needs to create ExceptionMapper for jersey.
	// throw WebServiceUtil.createWebserviceException(e);
	// }
	// }
}
