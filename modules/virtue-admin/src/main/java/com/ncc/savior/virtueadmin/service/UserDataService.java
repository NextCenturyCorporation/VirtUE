package com.ncc.savior.virtueadmin.service;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import com.ncc.savior.virtueadmin.data.ITemplateManager;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.VirtueInstance;
import com.ncc.savior.virtueadmin.model.VirtueTemplate;
import com.ncc.savior.virtueadmin.model.VirtueUser;
import com.ncc.savior.virtueadmin.security.UserService;
import com.ncc.savior.virtueadmin.util.SaviorException;
import com.ncc.savior.virtueadmin.virtue.IActiveVirtueManager;

public class UserDataService {
	private IActiveVirtueManager activeVirtueManager;
	private ITemplateManager templateManager;

	public UserDataService(IActiveVirtueManager activeVirtueManager, ITemplateManager templateManager) {
		this.activeVirtueManager = activeVirtueManager;
		this.templateManager = templateManager;
	}

	public ApplicationDefinition getApplication(String appId) {
		// TODO should users be restricted to which applications they can see?
		verifyAndReturnUser();
		Optional<ApplicationDefinition> app = this.templateManager.getApplicationDefinition(appId);
		return app.orElse(null);
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
		VirtueUser user = UserService.getCurrentUser();
		if (!user.getAuthorities().contains("ROLE_USER")) {
			throw new SaviorException(SaviorException.UNKNOWN_ERROR, "User did not have USER role");
		}
		return user;
	}
}
