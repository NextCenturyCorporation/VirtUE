package com.ncc.savior.virtueadmin.model;

/**
 * Class to represent a user that has been authenticated by the security
 * service. What we want in here is TDB.
 * 
 *
 */
public class User {
	private static User testUser;
	private String username;

	static {
		testUser = new User();
		testUser.username = "testUser";
	}

	public static User testUser() {
		return testUser;
	}

	public String getUsername() {
		return username;
	}
}
