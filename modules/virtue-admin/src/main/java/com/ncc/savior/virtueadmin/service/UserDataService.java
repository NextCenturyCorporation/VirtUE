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
package com.ncc.savior.virtueadmin.service;

import java.util.Collection;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.ncc.savior.util.SaviorErrorCode;
import com.ncc.savior.util.SaviorException;
import com.ncc.savior.virtueadmin.data.ITemplateManager;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.VirtueInstance;
import com.ncc.savior.virtueadmin.model.VirtueTemplate;
import com.ncc.savior.virtueadmin.model.VirtueUser;
import com.ncc.savior.virtueadmin.security.SecurityUserService;
import com.ncc.savior.virtueadmin.virtue.IActiveVirtueManager;

/**
 * Service that provides functions for a user, mostly to retrieve that user's
 * data. All functions require ROLE_USER.
 */
public class UserDataService {
	private IActiveVirtueManager activeVirtueManager;
	private ITemplateManager templateManager;

	@Autowired
	private SecurityUserService securityService;

	public UserDataService(IActiveVirtueManager activeVirtueManager, ITemplateManager templateManager) {
		this.activeVirtueManager = activeVirtueManager;
		this.templateManager = templateManager;
	}

	public ApplicationDefinition getApplication(String appId) {
		// TODO should users be restricted to which applications they can see?
		verifyAndReturnUser();
		ApplicationDefinition app = this.templateManager.getApplicationDefinition(appId);
		return app;
	}

	public VirtueTemplate getVirtueTemplate(String templateId) {
		VirtueUser user = verifyAndReturnUser();
		VirtueTemplate vt = this.templateManager.getVirtueTemplateForUser(user, templateId);
		return vt;
	}

	public Collection<VirtueTemplate> getVirtueTemplatesForUser() {
		VirtueUser user = verifyAndReturnUser();
		Map<String, VirtueTemplate> vts = this.templateManager.getVirtueTemplatesForUser(user);
		return vts.values();
	}

	public Collection<VirtueInstance> getVirtueInstancesForUser() {
		VirtueUser user = verifyAndReturnUser();
		Collection<VirtueInstance> activeVirtues = activeVirtueManager.getVirtuesForUser(user);
		return activeVirtues;
	}

	public VirtueInstance getVirtueInstanceForUserById(String instanceId) {
		VirtueUser user = verifyAndReturnUser();
		VirtueInstance vi = activeVirtueManager.getVirtueForUserFromTemplateId(user, instanceId);
		return vi;
	}

	// create virtue is found in DesktopVirtueService

	// Launch virtue is currently unnecessary

	// Stop virtue not yet supported

	// Destroy virtue is found in DesktopVirtueService

	// Launch Virtue application found in DesktopVirtueService

	// Stop running virtue application not yet supported

	private VirtueUser verifyAndReturnUser() {
		VirtueUser user = securityService.getCurrentUser();
		if (!user.getAuthorities().contains(VirtueUser.ROLE_USER)) {
			throw new SaviorException(SaviorErrorCode.USER_NOT_AUTHORIZED, "User did not have USER role");
		}
		return user;
	}
}
