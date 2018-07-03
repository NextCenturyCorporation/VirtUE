package com.ncc.savior.virtueadmin.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.ncc.savior.util.SaviorException;
import com.ncc.savior.virtueadmin.data.IUserManager;
import com.ncc.savior.virtueadmin.model.VirtueUser;

/**
 * User Service provides the {@link VirtueUser} object from the Spring Security
 * modules. This is the instance that should be used by the rest of the system
 * to get the user.
 */
public class SecurityUserService {

	@Autowired
	private IUserManager userManager;

	public VirtueUser getCurrentUser() {
		return getUserFromSpringContext();
	}

	private VirtueUser getUserFromSpringContext() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth != null) {
			// Collection<? extends GrantedAuthority> groups = auth.getAuthorities();
			// Collection<String> myGroups = new ArrayList<String>(groups.size());
			// for (GrantedAuthority group : groups) {
			// String groupStr = group.getAuthority();
			// myGroups.add(groupStr);
			// }
			String name = auth.getName();
			VirtueUser user = userManager.getUser(name);
			if (user == null) {
				throw new SaviorException(SaviorException.USER_NOT_FOUND,
						"User=" + name + " not found in user database!");
			} else if (!user.isEnabled()) {
				throw new SaviorException(SaviorException.USER_DISABLED, "User=" + name + " is disabled!");
			}
			return user;
		}
		return VirtueUser.anonymousUser();
	}

}
