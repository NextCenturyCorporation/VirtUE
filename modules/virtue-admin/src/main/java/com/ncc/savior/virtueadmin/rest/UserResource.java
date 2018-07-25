package com.ncc.savior.virtueadmin.rest;

import java.util.Collection;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.springframework.beans.factory.annotation.Autowired;

import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.VirtueInstance;
import com.ncc.savior.virtueadmin.model.VirtueTemplate;
import com.ncc.savior.virtueadmin.service.DesktopVirtueService;
import com.ncc.savior.virtueadmin.service.UserDataService;

/**
 * Rest resource that handles endpoints specifically to satisfy API for a given
 * user.
 * 
 */

@Path("/user")
public class UserResource {

	@Autowired
	private DesktopVirtueService desktopService;

	@Autowired
	private UserDataService userVirtueService;

	// JHU - User API - application get
	@GET
	@Produces("application/json")
	@Path("application/{appId}")
	public ApplicationDefinition getApplicationById(@PathParam("appId") String appId) {
		ApplicationDefinition app = userVirtueService.getApplication(appId);
		return app;
	}

	// JHU - User API - role get
	@GET
	@Produces("application/json")
	@Path("virtue/template/{templateId}")
	public VirtueTemplate getVirtueTemplateByUser(@PathParam("templateId") String templateId) {
		VirtueTemplate vt = userVirtueService.getVirtueTemplate(templateId);
		return vt;
	}

	// JHU - User API - user role list
	@GET
	@Produces("application/json")
	@Path("virtue/template")
	public Collection<VirtueTemplate> getAllVirtueTemplatesByUser() {
		Collection<VirtueTemplate> vts = userVirtueService.getVirtueTemplatesForUser();
		return vts;
	}

	// JHU - User API - virtue get
	@GET
	@Produces("application/json")
	@Path("virtue/{virtueId}")
	public VirtueInstance getVirtueInstance(@PathParam("virtueId") String virtueId) {
		VirtueInstance vi = userVirtueService.getVirtueInstanceForUserById(virtueId);
		return vi;
	}

	// JHU - User API - user virtue list
	@GET
	@Produces("application/json")
	@Path("virtue")
	public Collection<VirtueInstance> getVirtueInstances() {
		Collection<VirtueInstance> vis = userVirtueService.getVirtueInstancesForUser();
		return vis;
	}

	// JHU - User API - virtue create
	@POST
	@Produces("application/json")
	@Path("virtue/template/{templateId}")
	public VirtueInstance createVirtue(@PathParam("templateId") String templateId) {
		VirtueInstance vi = desktopService.createVirtue(templateId);
		return vi;
	}

	// JHU - User API - virtue launch
	@POST
	@Produces("application/json")
	@Path("virtue/{virtueId}")
	public void launchVirtue(@PathParam("virtueId") String virtueId) {
		desktopService.startVirtue(virtueId);
	}

	// JHU - User API - virtue stop
	@POST
	@Produces("application/json")
	@Path("virtue/{virtueId}/stop")
	public void stopVirtue(@PathParam("virtueId") String virtueId) {
		desktopService.stopVirtue(virtueId);
	}

	// JHU - User API - virtue destroy
	@DELETE
	@Produces("application/json")
	@Path("virtue/{virtueId}")
	public void deleteVirtue(@PathParam("virtueId") String virtueId) {
		desktopService.deleteVirtue(virtueId);
	}

	// JHU - User API - virtue application launch
	@POST
	@Produces("application/json")
	@Path("virtue/{virtueId}/{applicationId}")
	public void startApplication(@PathParam("virtueId") String virtueId,
			@PathParam("applicationId") String applicationId) {
		desktopService.startApplication(virtueId, applicationId);
	}

	// JHU - User API - virtue application stop
	// Stop running virtue application not yet supported
	@DELETE
	@Produces("application/json")
	@Path("virtue/{virtueId}/{applicationId}")
	public void stopApplication(@PathParam("virtueId") String virtueId,
			@PathParam("applicationId") String applicationId) {
		desktopService.stopApplication(virtueId, applicationId);
	}
}
