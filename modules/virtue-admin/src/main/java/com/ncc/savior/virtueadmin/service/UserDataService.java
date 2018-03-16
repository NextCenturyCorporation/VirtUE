package com.ncc.savior.virtueadmin.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;

import com.ncc.savior.virtueadmin.data.ITemplateManager;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.security.SecurityUserService;
import com.ncc.savior.virtueadmin.util.SaviorException;
import com.ncc.savior.virtueadmin.virtue.IActiveVirtueManager;

import dto.VirtueTemplateDto;
import persistance.JpaVirtueInstance;
import persistance.JpaVirtueTemplate;
import persistance.JpaVirtueUser;

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
		Optional<ApplicationDefinition> app = this.templateManager.getApplicationDefinition(appId);
		return app.orElse(null);
	}

	public VirtueTemplateDto getVirtueTemplate(String templateId) {
		JpaVirtueUser user = verifyAndReturnUser();
		JpaVirtueTemplate jpaTemplate = this.templateManager.getVirtueTemplateForUser(user, templateId);
		VirtueTemplateDto vt = new VirtueTemplateDto(jpaTemplate);
		return vt;
	}

	public Collection<VirtueTemplateDto> getVirtueTemplatesForUser() {
		JpaVirtueUser user = verifyAndReturnUser();
		Map<String, JpaVirtueTemplate> jpaMap = this.templateManager.getVirtueTemplatesForUser(user);
		Map<String, VirtueTemplateDto> vts = new HashMap<String, VirtueTemplateDto>();
		for (Entry<String, JpaVirtueTemplate> entry : jpaMap.entrySet()) {
			vts.put(entry.getKey(), new VirtueTemplateDto(entry.getValue()));
		}
		return vts.values();
	}

	public Collection<JpaVirtueInstance> getVirtueInstancesForUser() {
		JpaVirtueUser user = verifyAndReturnUser();
		Collection<JpaVirtueInstance> activeVirtues = activeVirtueManager.getVirtuesForUser(user);
		return activeVirtues;
	}

	public JpaVirtueInstance getVirtueInstanceForUserById(String instanceId) {
		JpaVirtueUser user = verifyAndReturnUser();
		JpaVirtueInstance vi = activeVirtueManager.getVirtueForUserFromTemplateId(user, instanceId);
		return vi;
	}

	// create virtue is found in DesktopVirtueService

	// Launch virtue is currently unnecessary

	// Stop virtue not yet supported

	// Destroy virtue is found in DesktopVirtueService

	// Launch Virtue application found in DesktopVirtueService

	// Stop running virtue application not yet supported

	private JpaVirtueUser verifyAndReturnUser() {
		JpaVirtueUser user = securityService.getCurrentUser();
		if (!user.getAuthorities().contains("ROLE_USER")) {
			throw new SaviorException(SaviorException.UNKNOWN_ERROR, "User did not have USER role");
		}
		return user;
	}
}
