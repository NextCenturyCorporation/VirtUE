/*
 * Copyright (C) 2019 Next Century Corporation
 * 
 * This file may be redistributed and/or modified under either the GPL
 * 2.0 or 3-Clause BSD license. In addition, the U.S. Government is
 * granted government purpose rights. For details, see the COPYRIGHT.TXT
 * file at the root of this project.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 * 
 * SPDX-License-Identifier: (GPL-2.0-only OR BSD-3-Clause)
 */
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

import io.swagger.v3.oas.annotations.Operation;

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
	@Operation(summary = "Get application definition", description = "Gets a specific application definition based on the ID in the path.")
	public ApplicationDefinition getApplicationById(@PathParam("appId") String appId) {
		ApplicationDefinition app = userVirtueService.getApplication(appId);
		return app;
	}

	// JHU - User API - role get
	@GET
	@Produces("application/json")
	@Path("virtue/template/{templateId}")
	@Operation(summary = "Get virtue template.", description = "Returns a virtue template if vailable for the user making the request.")
	public VirtueTemplate getVirtueTemplateByUser(@PathParam("templateId") String templateId) {
		VirtueTemplate vt = userVirtueService.getVirtueTemplate(templateId);
		return vt;
	}

	// JHU - User API - user role list
	@GET
	@Produces("application/json")
	@Path("virtue/template")
	@Operation(summary = "Get virtue templates.", description = "Returns a list of all virtue templates available for the user making the request.")
	public Collection<VirtueTemplate> getAllVirtueTemplatesByUser() {
		Collection<VirtueTemplate> vts = userVirtueService.getVirtueTemplatesForUser();
		return vts;
	}

	// JHU - User API - virtue get
	@GET
	@Produces("application/json")
	@Path("virtue/{virtueId}")
	@Operation(summary = "Get active virtue instance.", description = "Returns an active virtue instance by ID if virtue instance is owned by user making the request.")
	public VirtueInstance getVirtueInstance(@PathParam("virtueId") String virtueId) {
		VirtueInstance vi = userVirtueService.getVirtueInstanceForUserById(virtueId);
		return vi;
	}

	// JHU - User API - user virtue list
	@GET
	@Produces("application/json")
	@Path("virtue")
	@Operation(summary = "Get all active virtue instances.", description = "Returns a list of all virtue instances owned by the user making the request.")
	public Collection<VirtueInstance> getVirtueInstances() {
		Collection<VirtueInstance> vis = userVirtueService.getVirtueInstancesForUser();
		return vis;
	}

	// JHU - User API - virtue create
	@POST
	@Produces("application/json")
	@Path("virtue/template/{templateId}")
	@Operation(summary = "create virtue instance.", description = "Creates a virtue instance from a virtue template for the user making the request.s")
	public VirtueInstance createVirtue(@PathParam("templateId") String templateId) {
		VirtueInstance vi = desktopService.createVirtue(templateId);
		return vi;
	}

	// JHU - User API - virtue launch
	@POST
	@Produces("application/json")
	@Path("virtue/{virtueId}")
	@Operation(summary = "Launch active virtue.", description = "Launch or start all the virtual machines in an existing virtue given by the virtue ID.")
	public void launchVirtue(@PathParam("virtueId") String virtueId) {
		desktopService.startVirtue(virtueId);
	}

	// JHU - User API - virtue stop
	@POST
	@Produces("application/json")
	@Path("virtue/{virtueId}/stop")
	@Operation(summary = "Stop active virtue.", description = "Stop all the virtual machines in an existing virtue given by the virtue ID.")
	public void stopVirtue(@PathParam("virtueId") String virtueId) {
		desktopService.stopVirtue(virtueId);
	}

	// JHU - User API - virtue destroy
	@DELETE
	@Produces("application/json")
	@Path("virtue/{virtueId}")
	@Operation(summary = "Delete active virtue.", description = "Delete all the virtual machines in an existing virtue given by the virtue ID and remove the virtue instance.")
	public void deleteVirtue(@PathParam("virtueId") String virtueId) {
		desktopService.deleteVirtue(virtueId);
	}

	// JHU - User API - virtue application launch
	@POST
	@Produces("application/json")
	@Path("virtue/{virtueId}/{applicationId}")
	@Operation(summary = "Start virtue application.", description = "Attempt to start an application given by application ID on a virtue instance given by virtueId.")
	public void startApplication(@PathParam("virtueId") String virtueId,
			@PathParam("applicationId") String applicationId) {
		desktopService.startApplication(virtueId, applicationId, null);
	}

	// JHU - User API - virtue application stop
	// Stop running virtue application not yet supported
	@DELETE
	@Produces("application/json")
	@Path("virtue/{virtueId}/{applicationId}")
	@Operation(hidden = true)
	public void stopApplication(@PathParam("virtueId") String virtueId,
			@PathParam("applicationId") String applicationId) {
		desktopService.stopApplication(virtueId, applicationId);
	}
}
