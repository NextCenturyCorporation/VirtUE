package com.ncc.savior.virtueadmin.service;

import java.util.List;

import com.ncc.savior.virtueadmin.data.IVirtueDataAccessObject;
import com.ncc.savior.virtueadmin.infrastructure.IInfrastructureService;
import com.ncc.savior.virtueadmin.model.Application;
import com.ncc.savior.virtueadmin.model.Role;
import com.ncc.savior.virtueadmin.model.User;
import com.ncc.savior.virtueadmin.model.Virtue;

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

	public List<Role> getRoles(User user) {
		List<Role> list = virtueDatabase.getRolesForUser(user);
		return list;
	}

	public List<Virtue> getVirtues(User user) {
		List<Virtue> list = virtueDatabase.getVirtuesForUser(user);
		return list;
	}

	public Virtue createVirtue(User user, String roleId) {
		Role role = virtueDatabase.getRole(roleId);
		// TODO somewhere test if user has role
		// TODO does user already have this provisioned?
		boolean useAlreadyProvisioned = true;
		// TODO what comes back when we provision a role?
		Object provisionResults = infrastructure.provisionRole(roleId, useAlreadyProvisioned);
		Virtue virtue = null;
		// TODO take provision results and convert to virtue.
		return virtue;
	}

	public Virtue launchVirtue(User user, String virtueId) {
		return null;
	}

	public Virtue stopVirtue(User user, String virtueId) {
		return null;
	}

	public void destroyVirtue(User user, String virtueId) {

	}

	public Application startApplication(User user, String virtueId, String applicationId) {
		return null;
	}

	public void stopApplication(User user, String virtueId, String applicationId) {

	}

}
