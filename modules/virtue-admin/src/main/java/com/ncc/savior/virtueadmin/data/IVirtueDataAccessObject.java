package com.ncc.savior.virtueadmin.data;

import java.util.List;

import com.ncc.savior.virtueadmin.model.Role;
import com.ncc.savior.virtueadmin.model.User;
import com.ncc.savior.virtueadmin.model.Virtue;

/**
 * Interface for virtues backend data store.
 * 
 *
 */
public interface IVirtueDataAccessObject {

	List<Role> getRolesForUser(User user);

	List<Virtue> getVirtuesForUser(User user);

	Role getRole(String roleId);

}
