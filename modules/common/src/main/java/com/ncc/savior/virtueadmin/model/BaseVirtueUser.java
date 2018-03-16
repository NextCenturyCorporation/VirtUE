package com.ncc.savior.virtueadmin.model;

import java.util.Collection;

import javax.persistence.ElementCollection;
import javax.persistence.Id;

/**
 * Class to represent a user that has been authenticated by the security
 * service. What we want in here is TDB.
 * 
 *
 */
public abstract class BaseVirtueUser {

	@Id
	protected String username;
	@ElementCollection(targetClass=String.class)
	protected Collection<String> authorities;

	protected BaseVirtueUser() {
		
	}

	public BaseVirtueUser(String name, Collection<String> authorities) {
		this.username = name;
		this.authorities = authorities;
	}

	public String getUsername() {
		return username;
	}

	public Collection<String> getAuthorities() {
		return authorities;
	}
}
