package com.ncc.savior.virtueadmin.service;

import java.util.List;

import com.ncc.savior.virtueadmin.data.IVirtueDataAccessObject;
import com.ncc.savior.virtueadmin.infrastructure.IInfrastructureService;
import com.ncc.savior.virtueadmin.model.Application;
import com.ncc.savior.virtueadmin.model.User;
import com.ncc.savior.virtueadmin.model.VirtueInstance;
import com.ncc.savior.virtueadmin.model.VirtueTemplate;

/**
 * Service to handle user available functions of the JHU API.
 * 
 *
 */
public class VirtueUserService {

	private IInfrastructureService infrastructure;
	private IVirtueDataAccessObject virtueDatabase;

	public VirtueUserService(IInfrastructureService infrastructure, IVirtueDataAccessObject virtueDatabase) {
		this.infrastructure = infrastructure;
		this.virtueDatabase = virtueDatabase;
	}

	public List<VirtueTemplate> getVirtueTemplates(User user, boolean expandIds) {
		List<VirtueTemplate> list = virtueDatabase.getTemplatesForUser(user);
		return list;
	}

	public List<VirtueInstance> getVirtues(User user) {
		List<VirtueInstance> list = virtueDatabase.getVirtuesForUser(user);
		return list;
	}

	public VirtueInstance getVirtue(User user, String virtueId) {
		// TODO more authorization checking
		VirtueInstance virtue = virtueDatabase.getVirtue(virtueId);
		if (virtue != null && virtue.getUsername().equals(user.getUsername())) {
			return virtue;
		} else {
			return null;
		}
	}

	public List<Application> getApplicationsForVirtue(User user, String virtueId) {
		List<Application> list = virtueDatabase.getApplicationsForVirtue(user, virtueId);
		return list;
	}

	public VirtueInstance createVirtue(User user, String templateId) {
		VirtueTemplate template = virtueDatabase.getTemplate(templateId);
		// TODO somewhere test if user has template
		// TODO does user already have this provisioned?
		boolean useAlreadyProvisioned = true;
		// TODO what comes back when we provision a template?
		VirtueInstance virtue = infrastructure.provisionTemplate(user, template, useAlreadyProvisioned);
		// TODO take provision results and convert to virtue.
		// tell DB that virtue has been created for user
		virtueDatabase.addVirtueForUser(user, virtue);

		return virtue;
	}

	public VirtueInstance launchVirtue(User user, String virtueId) {
		VirtueInstance virtue = virtueDatabase.getVirtue(virtueId);
		if (infrastructure.launchVirtue(virtue)) {
			return virtue;
		} else {
			// TODO better exception
			throw new RuntimeException("Virtue cannot be launched");
		}
	}

	public VirtueInstance stopVirtue(User user, String virtueId) {
		VirtueInstance virtue = virtueDatabase.getVirtue(virtueId);
		if (infrastructure.stopVirtue(virtue)) {
			return virtue;
		} else {
			// TODO better exception
			throw new RuntimeException("Virtue cannot be stopped");
		}
	}

	public void destroyVirtue(User user, String virtueId) {
		VirtueInstance virtue = virtueDatabase.getVirtue(virtueId);
		infrastructure.destroyVirtue(virtue);
	}

	public Application startApplication(User user, String virtueId, String applicationId) {
		return null;
	}

	public void stopApplication(User user, String virtueId, String applicationId) {

	}

}
