package com.ncc.savior.virtueadmin.data;

import java.util.List;

import com.ncc.savior.virtueadmin.model.Role;
import com.ncc.savior.virtueadmin.model.User;
import com.ncc.savior.virtueadmin.model.Virtue;

/**
 * Virtue Backend data store implementation using in-memory java data
 * structures. This should not be used in production and is for testing only.
 * 
 *
 */
public class InMemoryVirtueDao implements IVirtueDataAccessObject {

	@Override
	public List<Role> getRolesForUser(User user) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Virtue> getVirtuesForUser(User user) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Role getRole(String roleId) {
		// TODO Auto-generated method stub
		return null;
	}

}
