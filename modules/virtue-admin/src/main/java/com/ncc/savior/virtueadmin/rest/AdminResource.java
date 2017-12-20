package com.ncc.savior.virtueadmin.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.springframework.beans.factory.annotation.Autowired;

import com.ncc.savior.virtueadmin.model.User;
import com.ncc.savior.virtueadmin.model.VirtueInstance;
import com.ncc.savior.virtueadmin.security.UserService;
import com.ncc.savior.virtueadmin.service.DesktopVirtueService;
import com.ncc.savior.virtueadmin.util.WebServiceUtil;

@Path("/admin")
public class AdminResource {

	@Autowired
	private DesktopVirtueService desktopService;

	public AdminResource() {

	}

	@GET
	@Produces("text/plain")
	public String getAllVirtueByUser() {
		return "Admin success";
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
	@Path("deletevirtue/instance/{instanceId}")
	public void deleteVirtue(@PathParam("instanceId") String instanceId) {
		try {
			User user = getUserFromSecurity();
			desktopService.deleteVirtue(user, instanceId);
		} catch (RuntimeException e) {
			// TODO fix createWebserviceException
			// Probably need to create our own exception
			// Needs to create ExceptionMapper for jersey.
			throw WebServiceUtil.createWebserviceException(e);
		}
	}

	private User getUserFromSecurity() {
		User user = UserService.getCurrentUser();

		return User.adminUser();
	}
}
