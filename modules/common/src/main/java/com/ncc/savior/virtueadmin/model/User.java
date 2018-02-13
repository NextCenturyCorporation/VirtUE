package com.ncc.savior.virtueadmin.model;

import java.util.ArrayList;
import java.util.Collection;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

/**
 * Class to represent a user that has been authenticated by the security
 * service. What we want in here is TDB.
 * 
 *
 */
@Entity
public class User {
	private static final User testUser;
	private static final User anonymousUser;	
	private static User adminUser;

	@Id
	private String username;
	@ElementCollection(targetClass=String.class)
	private Collection<String> authorities;

	static {
		testUser = new User("testUser", new ArrayList<String>());
		anonymousUser = new User("anonymous", new ArrayList<String>());
		ArrayList<String> adminAuths = new ArrayList<String>();
		adminAuths.add("ROLE_ADMIN");
		adminAuths.add("ROLE_USER");
		adminUser = new User("admin", adminAuths);
		
	}
	
	protected User() {
		
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

	public static User adminUser() {
		return adminUser;
	}

	@Override
	public String toString() {
		return "User [username=" + username + ", authorities=" + authorities + "]";
	}
	
	
}
