package com.ncc.savior.virtueadmin.data;

import persistance.JpaVirtueUser;

/**
 * Instance of this class will manage a set of users for the system.
 */
public interface IUserManager {

	void addUser(JpaVirtueUser user);

	JpaVirtueUser getUser(String username);

	Iterable<JpaVirtueUser> getAllUsers();

	void clear();

	void removeUser(JpaVirtueUser user);

	void removeUser(String usernameToRemove);

}
