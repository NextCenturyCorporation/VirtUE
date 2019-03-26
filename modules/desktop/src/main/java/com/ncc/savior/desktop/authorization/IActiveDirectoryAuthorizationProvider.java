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

import javax.ws.rs.client.Invocation.Builder;

/**
 * Provides authorization services for the application. Implementations are
 * often platform specific.
 *
 *
 */
public interface IActiveDirectoryAuthorizationProvider {

	/**
	 * Returns the currently logged in user for the application. If possible, check
	 * the OS to see if we can use the OS's logged in user.
	 *
	 * @return
	 * @throws InvalidUserLoginException
	 */
	DesktopUser getCurrentUser() throws InvalidUserLoginException;

	/**
	 * Attempt to login to a new user.
	 *
	 * @param domain
	 *            - can be null for no domain, but not implementations will support
	 *            that feature.
	 * @param username
	 * @param password
	 * @return
	 * @throws InvalidUserLoginException
	 */
	DesktopUser login(String domain, String username, String password) throws InvalidUserLoginException;

	/**
	 * Returns current token for single sign on.
	 *
	 * @return
	 * @throws InvalidUserLoginException
	 */
	byte[] getCurrentToken(String serverPrinc) throws InvalidUserLoginException;

	/**
	 * Attempt to logout of the current user. This is not guaranteed to log out
	 * particularly in cases where the user is logged into the OS itself.
	 */
	void logout();

	void addAuthorizationTicket(Builder builder, String targetHost) throws InvalidUserLoginException;
}
