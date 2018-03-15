package com.ncc.savior.virtueadmin.data;

import com.ncc.savior.virtueadmin.model.VirtueUser;

/**
 * Instance of this class will manage a set of users for the system.
 */
public interface IUserManager {

	void addUser(VirtueUser user);

	VirtueUser getUser(String username);

	Iterable<VirtueUser> getAllUsers();

	void clear();

	void removeUser(VirtueUser user);

	void removeUser(String usernameToRemove);

}
