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

import java.util.Base64;

import javax.ws.rs.client.Invocation.Builder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import waffle.windows.auth.IWindowsCredentialsHandle;
import waffle.windows.auth.IWindowsIdentity;
import waffle.windows.auth.IWindowsImpersonationContext;
import waffle.windows.auth.impl.WindowsAccountImpl;
import waffle.windows.auth.impl.WindowsAuthProviderImpl;
import waffle.windows.auth.impl.WindowsCredentialsHandleImpl;
import waffle.windows.auth.impl.WindowsSecurityContextImpl;

public class WaffleWindowsActiveDirectoryAuthorizationProvider implements IActiveDirectoryAuthorizationProvider {
	private static final Logger logger = LoggerFactory
			.getLogger(WaffleWindowsActiveDirectoryAuthorizationProvider.class);
	private static final String HEADER_AUTHORIZATION = "Authorization";
	private static final String DEFAULT_SECURITY_PACKAGE = "Negotiate";
	private WindowsAuthProviderImpl auth;

	private IWindowsIdentity impersonatedUser;

	public WaffleWindowsActiveDirectoryAuthorizationProvider() {
		auth = new WindowsAuthProviderImpl();
	}

	@Override
	public DesktopUser getCurrentUser() {
		IWindowsImpersonationContext imp = null;
		if (impersonatedUser != null) {
			imp = impersonatedUser.impersonate();
		}
		String fqd = WindowsAccountImpl.getCurrentUsername();

		DesktopUser user = DesktopUser.fromFullyQualifiedDomainName(fqd);
		if (imp != null) {
			imp.revertToSelf();
		}
		return user;

	}

	@Override
	public DesktopUser login(String domain, String username, String password) {
		logout();
		impersonatedUser = auth.logonDomainUser(username, domain, password);
		IWindowsImpersonationContext imp = null;
		imp = impersonatedUser.impersonate();
		logger.debug("impersonating new user: " + WindowsAccountImpl.getCurrentUsername());
		DesktopUser user = new DesktopUser(domain, username);
		imp.revertToSelf();
		return user;
	}

	@Override
	public void logout() {
		if (impersonatedUser != null) {
			impersonatedUser.dispose();
			impersonatedUser = null;
		}
	}

	@Override
	public byte[] getCurrentToken(String serverPrinc) {
		try {
			IWindowsImpersonationContext imp = null;
			String current = WindowsAccountImpl.getCurrentUsername();
			if (impersonatedUser != null) {
				imp = impersonatedUser.impersonate();
				// impersonatedUser.get
			}
			IWindowsCredentialsHandle clientCredentials = WindowsCredentialsHandleImpl
					.getCurrent(DEFAULT_SECURITY_PACKAGE);
			clientCredentials.initialize();
			WindowsSecurityContextImpl clientContext = new WindowsSecurityContextImpl();
			clientContext.setPrincipalName(current);
			clientContext.setCredentialsHandle(clientCredentials);
			clientContext.setSecurityPackage(DEFAULT_SECURITY_PACKAGE);
			clientContext.initialize(null, null, serverPrinc);
			byte[] token = clientContext.getToken();
			if (imp != null) {
				imp.revertToSelf();
			}
			return token;
		} catch (Exception e) {
			logger.error("temp error", e);
			return null;
		}
	}

	private String getAuthorizationTicket(String targetHost) {
		if (null == targetHost || targetHost.trim().isEmpty()) {
			return null;
		}
		String serverPrinc = "HTTP/" + targetHost;
		// byte[] token2 =
		// WindowsSecurityContextImpl.getCurrent(DEFAULT_SECURITY_PACKAGE, "HTTP/" +
		// targetHost)
		// .getToken();
		byte[] token = getCurrentToken(serverPrinc);
		byte[] encoded = Base64.getEncoder().encode(token);
		String encodedStr = new String(encoded);
		return DEFAULT_SECURITY_PACKAGE + " " + encodedStr;
	}

	@Override
	public void addAuthorizationTicket(Builder builder, String targetHost) {
		String ticket = getAuthorizationTicket(targetHost);
		if (ticket != null) {
			builder.header(HEADER_AUTHORIZATION, ticket);
		}
	}

}
