package com.ncc.savior.virtueadmin.security;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.ncc.savior.virtueadmin.model.User;

/**
 * User Service provides the {@link User} object from the Spring Security
 * modules. This is the instance that should be used by the rest of the system
 * to get the user.
 * 
 *
 */
public class UserService {

	public static User getCurrentUser() {
		// TODO hook up to Spring Security at some point.
		// return User.testUser();
		return getUserFromSpringContext();
	}

	private static User getUserFromSpringContext() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth != null) {
			Collection<? extends GrantedAuthority> groups = auth.getAuthorities();
			Collection<String> myGroups = new ArrayList<String>(groups.size());
			for (GrantedAuthority group : groups) {
				String groupStr = group.getAuthority();
				myGroups.add(groupStr);
			}
			String authName = auth.getName();
			String name=authName;
			if (authName.indexOf("@")!=-1) {
				name=authName.substring(0,authName.indexOf("@"));
			}
			User user = new User(name, myGroups);
			return user;
		}
		return User.anonymousUser();
	}

}
