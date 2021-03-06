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
package com.ncc.savior.desktop.authorization;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.client.Invocation.Builder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.util.JavaUtil;
import com.ncc.savior.virtueadmin.model.OS;

public class AuthorizationService {
	private static final Logger logger = LoggerFactory.getLogger(AuthorizationService.class);

	private OS os;
	private IActiveDirectoryAuthorizationProvider authProvider;
	private String requiredDomain;
	private String loginUrl;
	private String logoutUrl;

	private Set<ILoginListener> loginListeners;

	public AuthorizationService(String requiredDomain, String loginUrl, String logoutUrl) {
		this.requiredDomain = requiredDomain;
		if (this.requiredDomain != null && this.requiredDomain.equals("")) {
			this.requiredDomain = null;
		}
		this.os = JavaUtil.getOs();
		this.loginUrl = loginUrl;
		this.logoutUrl = logoutUrl;
		this.loginListeners = new HashSet<ILoginListener>();
		createAuthProviderChain();
	}

	public AuthorizationService(String loginUrl, String logoutUrl) {
		this(null, loginUrl, logoutUrl);
	}

	private void createAuthProviderChain() {
		switch (os) {
		case WINDOWS:
			authProvider = new WaffleWindowsActiveDirectoryAuthorizationProvider();
			break;
		case MAC:
		case LINUX:
			// return new UsernamePasswordKerberosAuthorizationService(loginUrl, logoutUrl);
			authProvider = new JavaGssActiveDirectoryAuthorizationProvider();
			break;
		}
		DesktopUser user = null;
		try {
			user = authProvider.getCurrentUser();
		} catch (InvalidUserLoginException e) {
			logger.warn(
					"Error testing Single-Sign-On authentication provider.  Falling back to username/password authentication.",
					e);
			authProvider = new UsernamePasswordKerberosAuthorizationService(loginUrl, logoutUrl);
			return;
		} catch (RuntimeException e) {
			logger.warn(
					"Error testing Single-Sign-On authentication provider.  Falling back to username/password authentication.",
					e);
			authProvider = new UsernamePasswordKerberosAuthorizationService(loginUrl, logoutUrl);
			return;
		}
		if (requiredDomain == null || user == null
				|| !requiredDomain.toUpperCase().equals(user.getDomain().toUpperCase())) {
			authProvider = new UsernamePasswordKerberosAuthorizationService(loginUrl, logoutUrl);
		}
	}

	public DesktopUser getUser() throws InvalidUserLoginException {
		DesktopUser user = authProvider.getCurrentUser();
		return user;
	}

	public DesktopUser login(String domain, String username, String password) throws InvalidUserLoginException {
		if (!(authProvider instanceof UsernamePasswordKerberosAuthorizationService)) {
			authProvider = new UsernamePasswordKerberosAuthorizationService(loginUrl, logoutUrl);
			// if (requiredDomain == null || requiredDomain.equals(domain)) {
			// return authProvider.login(domain, username, password);
			// } else {
			// String msg = "Cannot login. Domain (" + domain + ") is not the required
			// domain (" + requiredDomain
			// + ")";
			// throw new RuntimeException(msg);
			// }
		}
		DesktopUser user = authProvider.login(domain, username, password);
		if (user != null) {
			triggerOnLogin(user);
		}
		return user;
	}

	public DesktopUser loginWithCachedCredentials() throws InvalidUserLoginException {
		DesktopUser user = getUser();
		triggerOnLogin(user);
		return user;
	}

	public void logout() {
		triggerOnLogout();
		authProvider.logout();
	}

	public String getRequiredDomain() {
		return requiredDomain;
	}

	public void addAuthorizationTicket(Builder builder, String targetHost) throws InvalidUserLoginException {
		authProvider.addAuthorizationTicket(builder, targetHost);
	}

	public void addLoginListener(ILoginListener listener) {
		loginListeners.add(listener);
	}

	public void removeLoginListener(ILoginListener listener) {
		loginListeners.remove(listener);
	}

	public void triggerOnLogin(DesktopUser user) {
		for (ILoginListener listener : loginListeners) {
			listener.onLogin(user);
		}
	}

	public void triggerOnLogout() {
		for (ILoginListener listener : loginListeners) {
			listener.onLogout();
		}
	}

	public static interface ILoginListener {
		public void onLogin(DesktopUser user);

		public void onLogout();
	}

}
