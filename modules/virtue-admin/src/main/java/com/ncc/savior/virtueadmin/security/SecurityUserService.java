/*
 * Copyright (C) 2019 Next Century Corporation
 * 
 * This file may be redistributed and/or modified under either the GPL
 * 2.0 or 3-Clause BSD license. In addition, the U.S. Government is
 * granted government purpose rights. For details, see the COPYRIGHT.TXT
 * file at the root of this project.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 * 
 * SPDX-License-Identifier: (GPL-2.0-only OR BSD-3-Clause)
 */
package com.ncc.savior.virtueadmin.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.ncc.savior.util.SaviorErrorCode;
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
				throw new SaviorException(SaviorErrorCode.USER_NOT_FOUND,
						"User=" + name + " not found in user database!");
			} else if (!user.isEnabled()) {
				throw new SaviorException(SaviorErrorCode.USER_DISABLED, "User=" + name + " is disabled!");
			}
			return user;
		}
		return VirtueUser.anonymousUser();
	}

}
