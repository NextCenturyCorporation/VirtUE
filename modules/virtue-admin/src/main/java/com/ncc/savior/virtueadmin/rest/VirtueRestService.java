package com.ncc.savior.virtueadmin.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/*
 * VirtueRestService class exposes api about a virtue. 
 * 
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "virtues")
@Path("/virtues")
public class VirtueRestService {

	public VirtueRestService() {
		// this.userService = userService;
	}

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String getHello() {
		return "Hello";
	}

	// @Autowired
	// private VirtueUserService userService;

	// This method returns all the virtues for a user with userToken
	// @GET
	// @Produces("application/json")
	// @Path("user/virtue")
	// public List<VirtueInstance> getAllVirtueByUser() throws URISyntaxException {
	// try {
	// User user = getUserFromSecurity();
	// // List<VirtueInstance> virtues = userService.getVirtues(user);
	// List<VirtueInstance> virtues =
	// userService.getVirtuesIncludingUnprovisioned(user);
	// return virtues;
	// } catch (RuntimeException e) {
	// // TODO fix createWebserviceException
	// // Probably need to create our own exception
	// // Needs to create ExceptionMapper for jersey.
	// throw WebServiceUtil.createWebserviceException(e);
	// }
	// }
	//
	// /**
	// * Path from API: user virtue template list
	// *
	// * @param userToken
	// * @return
	// * @throws URISyntaxException
	// */
	// @GET
	// @Produces("application/json")
	// @Path("user/virtue/template")
	// public List<VirtueTemplate> getTemplatesForUser(@QueryParam("expand") boolean
	// expandIds) {
	// try {
	// User user = getUserFromSecurity();
	// List<VirtueTemplate> templates = userService.getVirtueTemplates(user,
	// expandIds);
	// return templates;
	// } catch (RuntimeException e) {
	// // TODO fix createWebserviceException
	// // Probably need to create our own exception
	// // Needs to create ExceptionMapper for jersey.
	// throw WebServiceUtil.createWebserviceException(e);
	// }
	// }
	//
	// @GET
	// @Produces("application/json")
	// @Path("user/virtue/{virtueId}")
	// public VirtueInstance getVirtue(@PathParam("virtueId") String virtueId) {
	// try {
	// User user = getUserFromSecurity();
	// VirtueInstance virtue = userService.getVirtue(user, virtueId);
	// return virtue;
	// } catch (RuntimeException e) {
	// // TODO fix createWebserviceException
	// // Probably need to create our own exception
	// // Needs to create ExceptionMapper for jersey.
	// throw WebServiceUtil.createWebserviceException(e);
	// }
	// }
	//
	// @GET
	// @Produces("application/json")
	// @Path("user/virtue/template/{templateId}/create")
	// public VirtueInstance createVirtue(@PathParam("templateId") String
	// templateId) {
	// try {
	// User user = getUserFromSecurity();
	// VirtueInstance virtue = userService.createVirtue(user, templateId);
	// return virtue;
	// } catch (RuntimeException e) {
	// // TODO fix createWebserviceException
	// // Probably need to create our own exception
	// // Needs to create ExceptionMapper for jersey.
	// throw WebServiceUtil.createWebserviceException(e);
	// }
	// }
	//
	// @GET
	// @Produces("application/json")
	// @Path("user/virtue/{virtueId}/launch")
	// public VirtueInstance launchVirtue(@PathParam("virtueId") String virtueId) {
	// try {
	// User user = getUserFromSecurity();
	// VirtueInstance virtue = userService.launchVirtue(user, virtueId);
	// return virtue;
	// } catch (RuntimeException e) {
	// // TODO fix createWebserviceException
	// // Probably need to create our own exception
	// // Needs to create ExceptionMapper for jersey.
	// throw WebServiceUtil.createWebserviceException(e);
	// }
	// }
	//
	// @GET
	// @Produces("application/json")
	// @Path("user/virtue/{virtueId}/stop")
	// public VirtueInstance stopVirtue(@PathParam("virtueId") String virtueId) {
	// try {
	// User user = getUserFromSecurity();
	// VirtueInstance virtue = userService.stopVirtue(user, virtueId);
	// return virtue;
	// } catch (RuntimeException e) {
	// // TODO fix createWebserviceException
	// // Probably need to create our own exception
	// // Needs to create ExceptionMapper for jersey.
	// throw WebServiceUtil.createWebserviceException(e);
	// }
	// }
	//
	// @GET
	// @Produces("application/json")
	// @Path("user/virtue/{virtueId}/destroy")
	// public void destroyVirtue(@PathParam("virtueId") String virtueId) {
	// try {
	// User user = getUserFromSecurity();
	// userService.destroyVirtue(user, virtueId);
	// } catch (RuntimeException e) {
	// // TODO fix createWebserviceException
	// // Probably need to create our own exception
	// // Needs to create ExceptionMapper for jersey.
	// throw WebServiceUtil.createWebserviceException(e);
	// }
	// }
	//
	// @GET
	// @Produces("application/json")
	// @Path("user/virtue/{virtueId}/{applicationId}/launch")
	// public ApplicationDefinition launchApplication(@PathParam("virtueId") String
	// virtueId,
	// @PathParam("applicationId") String applicationId) {
	// try {
	// User user = getUserFromSecurity();
	// ApplicationDefinition app = userService.startApplication(user, virtueId,
	// applicationId);
	// return app;
	// } catch (RuntimeException e) {
	// // TODO fix createWebserviceException
	// // Probably need to create our own exception
	// // Needs to create ExceptionMapper for jersey.
	// throw WebServiceUtil.createWebserviceException(e);
	// }
	// }
	//
	// @GET
	// @Produces("application/json")
	// @Path("user/virtue/{virtueId}/{applicationId}/stop")
	// public void stopApplication(@PathParam("virtueId") String virtueId,
	// @PathParam("applicationId") String applicationId) {
	// try {
	// User user = getUserFromSecurity();
	// userService.stopApplication(user, virtueId, applicationId);
	// } catch (RuntimeException e) {
	// // TODO fix createWebserviceException
	// // Probably need to create our own exception
	// // Needs to create ExceptionMapper for jersey.
	// throw WebServiceUtil.createWebserviceException(e);
	// }
	// }

	// private User getUserFromSecurity() {
	// // TODO hook up to Spring Security at some point.
	// return User.testUser();
	// }
}
