package com.ncc.savior.virtueadmin.security;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.ncc.savior.virtueadmin.model.User;

public class UserService {

	public static User getCurrentUser() {
		// TODO hook up to Spring Security at some point.
		// return User.testUser();
		return getUserFromSpringContext();
	}

	private static User getUserFromSpringContext() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		Collection<? extends GrantedAuthority> groups = auth.getAuthorities();
		Collection<String> myGroups = new ArrayList<String>(groups.size());
		for (GrantedAuthority group : groups) {
			String groupStr = group.getAuthority();
			myGroups.add(groupStr);
		}
		User user = new User(auth.getName(), myGroups);
		return user;
	}

}
