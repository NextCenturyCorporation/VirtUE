package com.ncc.savior.virtueadmin.rest;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.springframework.beans.factory.annotation.Autowired;

import com.ncc.savior.virtueadmin.model.IconModel;
import com.ncc.savior.virtueadmin.model.VirtueInstance;
import com.ncc.savior.virtueadmin.model.desktop.DesktopVirtue;
import com.ncc.savior.virtueadmin.model.desktop.DesktopVirtueApplication;
import com.ncc.savior.virtueadmin.service.DesktopVirtueService;

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
		Set<DesktopVirtue> virtues = desktopService.getDesktopVirtuesForUser();
		return virtues;
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
	public DesktopVirtueApplication startApplication(@PathParam("virtueId") String virtueId,
			@PathParam("applicationId") String applicationId) {
		return desktopService.startApplication(virtueId, applicationId);
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
	public DesktopVirtueApplication startApplicationFromTemplate(@PathParam("templateId") String templateId,
			@PathParam("applicationId") String applicationId) {
		return desktopService.startApplicationFromTemplate(templateId, applicationId);
	}

	@GET
	@Produces("application/json")
	@Path("template/{templateId}/start")
	public DesktopVirtue createVirtue(@PathParam("templateId") String templateId) {
		return desktopService.createVirtueAsDesktopVirtue(templateId);
	}

	@GET
	@Produces("application/json")
	@Path("virtue/{virtueId}/start")
	public DesktopVirtue startVirtue(@PathParam("virtueId") String virtueId) {
		return desktopService.startVirtue(virtueId);
	}

	@GET
	@Produces("application/json")
	@Path("virtue/{virtueId}/stop")
	public DesktopVirtue stopVirtue(@PathParam("virtueId") String virtueId) {
		return desktopService.stopVirtue(virtueId);
	}

	@GET
	@Produces({ "image/png", "image/jpeg" })
	@Path("icon/{iconKey}")
	public byte[] getIcon(@PathParam("iconKey") String iconKey) {
		IconModel iconModel = desktopService.getIcon(iconKey);
		return iconModel.getData();
	}
	
	@GET
	@Path("virtue/{virtueId}/terminate")
	public DesktopVirtue terminateVirtue(@PathParam("virtueId") String virtueId) {
		return desktopService.terminateVirtue(virtueId);
	}

}
