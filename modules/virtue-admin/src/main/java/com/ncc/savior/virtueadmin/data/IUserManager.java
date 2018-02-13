package com.ncc.savior.virtueadmin.data;

import com.ncc.savior.virtueadmin.model.User;

public interface IUserManager {

	void addUser(User admin);

	User getUser(String username);

	Iterable<User> getAllUsers();

	void clear();

}
