package com.ncc.savior.virtueadmin.model;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Class to represent a user that has been authenticated by the security
 * service. What we want in here is TDB.
 * 
 *
 */
public class User {
	private static User testUser;
	private static User anonymousUser;
	private String username;
	private Collection<String> authorities;

	static {
		testUser = new User("testUser", new ArrayList<String>());
		anonymousUser = new User("anonymous", new ArrayList<String>());
	}

	public User(String name, Collection<String> authorities) {
		this.username = name;
		this.authorities = authorities;
	}

	public static User testUser() {
		return testUser;
	}

	public String getUsername() {
		return username;
	}

	public Collection<String> getAuthorities() {
		return authorities;
	}

	public static User anonymousUser() {
		return anonymousUser;
	}
}
