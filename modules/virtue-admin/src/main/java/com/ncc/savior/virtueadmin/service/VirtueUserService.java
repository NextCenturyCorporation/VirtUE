package com.ncc.savior.virtueadmin.service;

import java.util.ArrayList;
import java.util.List;

import com.ncc.savior.virtueadmin.data.IVirtueDataAccessObject;
import com.ncc.savior.virtueadmin.infrastructure.IInfrastructureService;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.User;
import com.ncc.savior.virtueadmin.model.VirtueInstance;
import com.ncc.savior.virtueadmin.model.VirtueState;
import com.ncc.savior.virtueadmin.model.VirtueTemplate;
import com.ncc.savior.virtueadmin.model.VmState;

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
		this.infrastructure.addStateUpdateListener(new StateUpdateListener());
	}

	public List<VirtueTemplate> getVirtueTemplates(User user, boolean expandIds) {
		List<VirtueTemplate> list = virtueDatabase.getTemplatesForUser(user);
		return list;
	}

	public List<VirtueInstance> getVirtues(User user) {
		List<VirtueInstance> list = virtueDatabase.getVirtuesForUser(user);
		return list;
	}

	public List<VirtueInstance> getVirtuesIncludingUnprovisioned(User user) {
		List<VirtueTemplate> templates = virtueDatabase.getTemplatesForUser(user);
		List<VirtueInstance> list = virtueDatabase.getVirtuesForUser(user);
		ArrayList<VirtueInstance> newInstances = new ArrayList<VirtueInstance>();
		for (VirtueTemplate template : templates) {
			boolean templateHasInstance = false;
			for (VirtueInstance instance : list) {
				if (instance.getTemplateid().equals(template.getId())) {
					templateHasInstance = true;
					break;
				}
			}
			if (!templateHasInstance) {
				VirtueInstance instance = new VirtueInstance(template, user.getUsername());
				newInstances.add(instance);
			}
		}

		for (VirtueInstance instance : newInstances) {
			virtueDatabase.addVirtueForUser(user, instance);
			list.add(instance);
		}
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

	public List<ApplicationDefinition> getApplicationsForVirtue(User user, String virtueId) {
		List<ApplicationDefinition> list = virtueDatabase.getApplicationsForVirtue(user, virtueId);
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
			virtue.setState(VirtueState.LAUNCHING);
			virtueDatabase.updateVirtueState(virtueId, VirtueState.LAUNCHING);
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

	public ApplicationDefinition startApplication(User user, String virtueId, String applicationId) {
		return null;
	}

	public void stopApplication(User user, String virtueId, String applicationId) {

	}

	/**
	 * Used to help with asynchronous actions
	 */
	public class StateUpdateListener {
		public void updateVirtueState(String virtueId, VirtueState state) {
			virtueDatabase.updateVirtueState(virtueId, state);
		}

		public void updateVmState(String virtueId, String vmId, VmState state) {
			virtueDatabase.updateVmState(virtueId, vmId, state);
		}
	}
}
