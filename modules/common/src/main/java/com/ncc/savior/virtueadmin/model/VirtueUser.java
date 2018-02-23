package com.ncc.savior.virtueadmin.model;

import java.util.ArrayList;
import java.util.Collection;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Class to represent a user that has been authenticated by the security
 * service. What we want in here is TDB.
 * 
 *
 */
@Entity
public class VirtueUser {
	private static final VirtueUser testUser;
	private static final VirtueUser anonymousUser;	
	private static VirtueUser adminUser;

	@Id
	private String username;
	@ElementCollection(targetClass=String.class)
	private Collection<String> authorities;

	static {
		testUser = new VirtueUser("testUser", new ArrayList<String>());
		anonymousUser = new VirtueUser("anonymous", new ArrayList<String>());
		ArrayList<String> adminAuths = new ArrayList<String>();
		adminAuths.add("ROLE_ADMIN");
		adminAuths.add("ROLE_USER");
		adminUser = new VirtueUser("admin", adminAuths);
		
	}
	
	protected VirtueUser() {
		
	}

	public VirtueUser(String name, Collection<String> authorities) {
		this.username = name;
		this.authorities = authorities;
	}

	public static VirtueUser testUser() {
		return testUser;
	}

	public String getUsername() {
		return username;
	}

	public Collection<String> getAuthorities() {
		return authorities;
	}

	public static VirtueUser anonymousUser() {
		return anonymousUser;
	}

	public static VirtueUser adminUser() {
		return adminUser;
	}

	@Override
	public String toString() {
		return "User [username=" + username + ", authorities=" + authorities + "]";
	}
	
	
}
