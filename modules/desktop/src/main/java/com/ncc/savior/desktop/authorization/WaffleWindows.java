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

import waffle.util.WaffleInfo;
import waffle.windows.auth.IWindowsAccount;
import waffle.windows.auth.IWindowsAuthProvider;
import waffle.windows.auth.IWindowsCredentialsHandle;
import waffle.windows.auth.impl.WindowsAccountImpl;
import waffle.windows.auth.impl.WindowsAuthProviderImpl;
import waffle.windows.auth.impl.WindowsCredentialsHandleImpl;
import waffle.windows.auth.impl.WindowsSecurityContextImpl;

//http://www.javahabit.com/2015/09/27/waffle-windows-single-sign-on/

/**
 * Waffle test application
 *
 *
 */
public class WaffleWindows {
	public static void main(String[] args) {
		final IWindowsAuthProvider auth = new WindowsAuthProviderImpl();
		WindowsAccountImpl.getCurrentUsername();
		IWindowsAccount a = auth.lookupAccount("");
		System.out.println(a.getFqn());
		test();

		WaffleInfo.main(new String[] { "-show" });
	}

	public static void test() {
		final String securityPackage = "Negotiate";
		IWindowsCredentialsHandle clientCredentials = null;
		WindowsSecurityContextImpl clientContext = null;
		try {
			// client credentials handle
			clientCredentials = WindowsCredentialsHandleImpl.getCurrent(securityPackage);
			clientCredentials.initialize();
			// initial client security context
			clientContext = new WindowsSecurityContextImpl();
			clientContext.setPrincipalName(WindowsAccountImpl.getCurrentUsername());
			clientContext.setCredentialsHandle(clientCredentials);
			clientContext.setSecurityPackage(securityPackage);
			clientContext.initialize(null, null, WindowsAccountImpl.getCurrentUsername());
			byte[] token = clientContext.getToken();
			final String clientToken = Base64.getEncoder().encodeToString(token);
			String authorizationHeader = securityPackage + " " + clientToken;
			System.out.println("Header=" + authorizationHeader);
		} finally {
			if (clientContext != null) {
				clientContext.dispose();
			}
			if (clientCredentials != null) {
				clientCredentials.dispose();
			}
		}
	}

	// public static void test2() {
	// String securityPackage = "Negotiate";
	// // client credentials handle
	// IWindowsCredentialsHandle clientCredentials =
	// WindowsCredentialsHandleImpl.getCurrent(securityPackage);
	// clientCredentials.initialize();
	// // initial client security context
	// WindowsSecurityContextImpl clientContext = new WindowsSecurityContextImpl();
	// clientContext.setPrincipalName(WindowsAccountImpl.getCurrentUsername());
	// clientContext.setCredentialsHandle(clientCredentials);
	// clientContext.setSecurityPackage(securityPackage);
	// clientContext.initialize(null, null,
	// WindowsAccountImpl.getCurrentUsername());
	// byte[] token = clientContext.getToken();
	// final String clientToken = Base64.getEncoder().encodeToString(token);
	// String authorizationHeader = securityPackage + " " + clientToken;
	//
	// // accept on the server
	// WindowsAuthProviderImpl provider = new WindowsAuthProviderImpl();
	// IWindowsSecurityContext serverContext = null;
	// do
	// {
	// if (serverContext != null) {
	// // initialize on the client
	// SecBufferDesc continueToken = new SecBufferDesc(Sspi.SECBUFFER_TOKEN,
	// serverContext.getToken());
	// clientContext.initialize();
	// }
	// // accept the token on the server
	// serverContext = provider.acceptSecurityToken(clientContext.getToken(),
	// securityPackage);
	// } while (clientContext.getContinue() || serverContext.getContinue());
	// System.out.println(serverContext.getIdentity().getFqn());
	// for (IWindowsAccount group : serverContext.getIdentity().getGroups()) {
	// System.out.println(" " + group.getFqn());
	// }
	// serverContext.dispose();
	// clientContext.dispose();
	// clientCredentials.dispose();
	// }
}
