package com.ncc.savior.virtueadmin.rest;

import java.io.IOException;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.springframework.beans.factory.annotation.Autowired;

import com.ncc.savior.virtueadmin.model.VirtueUser;
import com.ncc.savior.virtueadmin.model.desktop.DesktopVirtue;
import com.ncc.savior.virtueadmin.model.desktop.IApplicationInstance;
import com.ncc.savior.virtueadmin.security.UserService;
import com.ncc.savior.virtueadmin.service.DesktopVirtueService;
import com.ncc.savior.virtueadmin.util.WebServiceUtil;

/**
 * Rest resource that handles endpoints specifically for the desktop
 * application.
 * 
 */

@Path("/desktop")
public class DesktopRestService {

	@Autowired
	private DesktopVirtueService desktopService;

	/**
	 * Gets all virtues as {@link DesktopVirtue}s for the user including Virtues
	 * that have not been provisioned yet, but the user has the ability to
	 * provision.
	 * 
	 * @return
	 * 
	 */
	@GET
	@Produces("application/json")
	@Path("virtue")
	public Set<DesktopVirtue> getAllVirtueByUser() {
		try {
			VirtueUser user = getUserFromSecurity();
			Set<DesktopVirtue> virtues = desktopService.getDesktopVirtuesForUser(user);
			return virtues;
		} catch (RuntimeException e) {
			// TODO fix createWebserviceException
			// Probably need to create our own exception
			// Needs to create ExceptionMapper for jersey.
			throw WebServiceUtil.createWebserviceException(e);
		}
	}

	/**
	 * Starts an application on a given virtue for the requesting user.
	 * 
	 * @param virtueId
	 * @param applicationId
	 * @return
	 */
	@GET
	@Produces("application/json")
	@Path("virtue/{virtueId}/{applicationId}/start")
	public IApplicationInstance startApplication(@PathParam("virtueId") String virtueId,
			@PathParam("applicationId") String applicationId) {
		try {
			VirtueUser user = getUserFromSecurity();
			return desktopService.startApplication(user, virtueId, applicationId);
		} catch (RuntimeException | IOException e) {
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
	@Path("template/{templateId}/{applicationId}/start")
	public IApplicationInstance startApplicationFromTemplate(@PathParam("templateId") String templateId,
			@PathParam("applicationId") String applicationId) {
		try {
			VirtueUser user = getUserFromSecurity();
			return desktopService.startApplicationFromTemplate(user, templateId, applicationId);
		} catch (RuntimeException | IOException e) {
			// TODO fix createWebserviceException
			// Probably need to create our own exception
			// Needs to create ExceptionMapper for jersey.
			throw WebServiceUtil.createWebserviceException(e);
		}
	}
	
	

	private VirtueUser getUserFromSecurity() {
		VirtueUser user = UserService.getCurrentUser();
		return user;
	}
}
