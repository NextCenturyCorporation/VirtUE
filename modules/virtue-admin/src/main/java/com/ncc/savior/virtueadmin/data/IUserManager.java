package com.ncc.savior.virtueadmin.data;

import com.ncc.savior.virtueadmin.model.VirtueUser;

/**
 * Instance of this class will manage a set of users for the system.
 */
public interface IUserManager {

	void addUser(VirtueUser user);

	VirtueUser getUser(String username);

	Iterable<VirtueUser> getAllUsers();

	/**
	 * Clears all virtues from all users. If removeAllUsers is true, it then deletes
	 * those users.
	 * 
	 * @param removeAllUsers
	 */
	void clear(boolean removeAllUsers);

	void removeUser(VirtueUser user);

	void removeUser(String usernameToRemove);

	boolean userExists(String username);

	void enableDisableUser(String username, Boolean enable);

}
