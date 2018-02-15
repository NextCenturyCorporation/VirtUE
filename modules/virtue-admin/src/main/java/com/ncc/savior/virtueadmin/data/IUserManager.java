package com.ncc.savior.virtueadmin.data;

import com.ncc.savior.virtueadmin.model.VirtueUser;

public interface IUserManager {

	void addUser(VirtueUser admin);

	VirtueUser getUser(String username);

	Iterable<VirtueUser> getAllUsers();

	void clear();

	void removeUser(VirtueUser user);

}
