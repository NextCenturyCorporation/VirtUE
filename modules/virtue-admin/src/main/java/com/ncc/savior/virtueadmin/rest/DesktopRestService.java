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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.springframework.beans.factory.annotation.Autowired;

import com.ncc.savior.virtueadmin.model.ClipboardPermission;
import com.ncc.savior.virtueadmin.model.ClipboardPermissionOption;
import com.ncc.savior.virtueadmin.model.IconModel;
import com.ncc.savior.virtueadmin.model.desktop.DesktopVirtue;
import com.ncc.savior.virtueadmin.model.desktop.DesktopVirtueApplication;
import com.ncc.savior.virtueadmin.service.DesktopVirtueService;
import com.ncc.savior.virtueadmin.service.PermissionService;

import io.swagger.v3.oas.annotations.Operation;

/**
 * Rest resource that handles endpoints specifically for the desktop
 * application.
 * 
 */

@Path("/desktop")
public class DesktopRestService {

	@Autowired
	private DesktopVirtueService desktopService;

	@Autowired
	private PermissionService permissionService;

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
	@Operation(hidden = true)
	public Set<DesktopVirtue> getAllVirtueByUser() {
		Set<DesktopVirtue> virtues = desktopService.getDesktopVirtuesForUser();
		return virtues;
	}

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
	@Path("applications/{tag}")
	@Operation(hidden = true)
	public Set<DesktopVirtue> getVirtueApplicationsByUserAndTag(@PathParam("tag") String tag) {
		Set<DesktopVirtue> virtues = desktopService.getVirtueApplicationsByUserAndTag(tag);
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
	@Operation(hidden = true)
	public DesktopVirtueApplication startApplication(@PathParam("virtueId") String virtueId,
			@PathParam("applicationId") String applicationId, @QueryParam("cliParams") String params) {
		return desktopService.startApplication(virtueId, applicationId, params);
	}

	/**
	 * Returns a list of "applications" to enable the desktop to reconnect to a
	 * virtue.
	 * 
	 * @param virtueId
	 * @return
	 */
	@GET
	@Produces("application/json")
	@Path("virtue/{virtueId}/reconnect")
	@Operation(hidden = true)
	public Collection<DesktopVirtueApplication> reconnectToVirtue(@PathParam("virtueId") String virtueId) {
		return desktopService.getReconnectApps(virtueId);
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
	@Operation(hidden = true)
	public DesktopVirtueApplication startApplicationFromTemplate(@PathParam("templateId") String templateId,
			@PathParam("applicationId") String applicationId) {
		return desktopService.startApplicationFromTemplate(templateId, applicationId);
	}

	@GET
	@Produces("application/json")
	@Path("template/{templateId}/start")
	@Operation(hidden = true)
	public DesktopVirtue createVirtue(@PathParam("templateId") String templateId) {
		return desktopService.createVirtueAsDesktopVirtue(templateId);
	}

	@GET
	@Produces("application/json")
	@Path("virtue/{virtueId}/start")
	@Operation(hidden = true)
	public DesktopVirtue startVirtue(@PathParam("virtueId") String virtueId) {
		return desktopService.startVirtue(virtueId);
	}

	@GET
	@Produces("application/json")
	@Path("virtue/{virtueId}/stop")
	@Operation(hidden = true)
	public DesktopVirtue stopVirtue(@PathParam("virtueId") String virtueId) {
		return desktopService.stopVirtue(virtueId);
	}

	@GET
	@Produces({ "image/png", "image/jpeg" })
	@Path("icon/{iconKey}")
	@Operation(hidden = true)
	public byte[] getIcon(@PathParam("iconKey") String iconKey) {
		IconModel iconModel = desktopService.getIcon(iconKey);
		return iconModel.getData();
	}

	@GET
	@Path("permissions")
	@Produces("application/json")
	@Operation(hidden = true)
	public List<ClipboardPermission> getAllComputedPermissions() {
		Iterable<DesktopVirtue> templates = desktopService.getDesktopVirtuesForUser();
		Collection<String> sourceIds = new ArrayList<String>();
		for (DesktopVirtue t : templates) {
			sourceIds.add(t.getTemplateId());
		}
		sourceIds.add(ClipboardPermission.DESKTOP_CLIENT_GROUP_ID);
		return permissionService.getAllPermissionsForSources(sourceIds);
	}

	@GET
	@Path("permissions/default")
	@Operation(hidden = true)
	public ClipboardPermissionOption getServiceDefaultPermission() {
		ClipboardPermissionOption option = permissionService.getDefaultClipboardPermission();
		return option;
	}

	@GET
	@Path("virtue/{virtueId}/terminate")
	@Operation(hidden = true)
	public DesktopVirtue terminateVirtue(@PathParam("virtueId") String virtueId) {
		return desktopService.terminateVirtue(virtueId);
	}
}
