package com.ncc.savior.virtueadmin.rest;

import java.io.IOException;
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
import com.ncc.savior.virtueadmin.util.SaviorException;
import com.ncc.savior.virtueadmin.util.WebServiceUtil;

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

	@GET
	@Produces("application/json")
	@Path("application/{appId}")
	public ApplicationDefinition getApplicationById(@PathParam("appId") String appId) {
		try {
			ApplicationDefinition app = userVirtueService.getApplication(appId);
			return app;
		} catch (RuntimeException e) {
			// TODO fix createWebserviceException
			// Probably need to create our own exception
			// Needs to create ExceptionMapper for jersey.
			throw WebServiceUtil.createWebserviceException(e);
		}
	}

	@GET
	@Produces("application/json")
	@Path("virtue/template/{templateId}")
	public VirtueTemplate getVirtueTemplateByUser(@PathParam("templateId") String templateId) {
		try {
			VirtueTemplate vt = userVirtueService.getVirtueTemplate(templateId);
			return vt;
		} catch (RuntimeException e) {
			// TODO fix createWebserviceException
			// Probably need to create our own exception
			// Needs to create ExceptionMapper for jersey.
			throw WebServiceUtil.createWebserviceException(e);
		}
	}

	@GET
	@Produces("application/json")
	@Path("virtue/template/")
	public Collection<VirtueTemplate> getAllVirtueTemplatesByUser() {
		try {
			Collection<VirtueTemplate> vts = userVirtueService.getVirtueTemplatesForUser();
			return vts;
		} catch (RuntimeException e) {
			// TODO fix createWebserviceException
			// Probably need to create our own exception
			// Needs to create ExceptionMapper for jersey.
			throw WebServiceUtil.createWebserviceException(e);
		}
	}

	@GET
	@Produces("application/json")
	@Path("virtue/{virtueId}/")
	public VirtueInstance getVirtueInstance(@PathParam("virtueId") String virtueId) {
		try {
			VirtueInstance vi = userVirtueService.getVirtueInstanceForUserById(virtueId);
			return vi;
		} catch (RuntimeException e) {
			// TODO fix createWebserviceException
			// Probably need to create our own exception
			// Needs to create ExceptionMapper for jersey.
			throw WebServiceUtil.createWebserviceException(e);
		}
	}

	@GET
	@Produces("application/json")
	@Path("virtue/")
	public Collection<VirtueInstance> getVirtueInstances() {
		try {
			Collection<VirtueInstance> vis = userVirtueService.getVirtueInstancesForUser();
			return vis;
		} catch (RuntimeException e) {
			// TODO fix createWebserviceException
			// Probably need to create our own exception
			// Needs to create ExceptionMapper for jersey.
			throw WebServiceUtil.createWebserviceException(e);
		}
	}

	@POST
	@Produces("application/json")
	@Path("virtue/template/{templateId}/")
	public VirtueInstance createVirtue(@PathParam("templateId") String templateId) {
		try {
			VirtueInstance vi = desktopService.createVirtue(templateId);
			return vi;
		} catch (RuntimeException e) {
			// TODO fix createWebserviceException
			// Probably need to create our own exception
			// Needs to create ExceptionMapper for jersey.
			throw WebServiceUtil.createWebserviceException(e);
		}
	}

	// Launch virtue is currently unnecessary
	@POST
	@Produces("application/json")
	@Path("virtue/{virtueId}/")
	public void launchVirtue(@PathParam("virtueId") String virtueId) {
		try {
			throw new SaviorException(SaviorException.NOT_YET_IMPLEMENTED,
					"Launch virtue not yet implemented.  Virtues are lauched when created.");
		} catch (RuntimeException e) {
			// TODO fix createWebserviceException
			// Probably need to create our own exception
			// Needs to create ExceptionMapper for jersey.
			throw WebServiceUtil.createWebserviceException(e);
		}
	}

	// Stop virtue not yet supported
	@POST
	@Produces("application/json")
	@Path("virtue/{virtueId}/stop")
	public void stopVirtue(@PathParam("virtueId") String virtueId) {
		try {
			throw new SaviorException(SaviorException.NOT_YET_IMPLEMENTED,
					"Stop virtue not yet implemented.  Virtues must be stopped and destroyed manually");
		} catch (RuntimeException e) {
			// TODO fix createWebserviceException
			// Probably need to create our own exception
			// Needs to create ExceptionMapper for jersey.
			throw WebServiceUtil.createWebserviceException(e);
		}
	}

	@DELETE
	@Produces("application/json")
	@Path("virtue/{virtueId}/")
	public void deleteVirtue(@PathParam("virtueId") String virtueId) {
		try {
			desktopService.deleteVirtue(virtueId);
		} catch (RuntimeException e) {
			// TODO fix createWebserviceException
			// Probably need to create our own exception
			// Needs to create ExceptionMapper for jersey.
			throw WebServiceUtil.createWebserviceException(e);
		}
	}

	@POST
	@Produces("application/json")
	@Path("virtue/{virtueId}/{applicationId}")
	public void startApplication(@PathParam("virtueId") String virtueId,
			@PathParam("applicationId") String applicationId) {
		try {
			desktopService.startApplication(virtueId, applicationId);
		} catch (RuntimeException | IOException e) {
			// TODO fix createWebserviceException
			// Probably need to create our own exception
			// Needs to create ExceptionMapper for jersey.
			throw WebServiceUtil.createWebserviceException(e);
		}
	}

	// Stop running virtue application not yet supported
	@DELETE
	@Produces("application/json")
	@Path("virtue/{virtueId}/{applicationId}")
	public void stopApplication(@PathParam("virtueId") String virtueId,
			@PathParam("applicationId") String applicationId) {
		try {
			desktopService.stopApplication(virtueId, applicationId);
		} catch (RuntimeException | IOException e) {
			// TODO fix createWebserviceException
			// Probably need to create our own exception
			// Needs to create ExceptionMapper for jersey.
			throw WebServiceUtil.createWebserviceException(e);
		}
	}
}
