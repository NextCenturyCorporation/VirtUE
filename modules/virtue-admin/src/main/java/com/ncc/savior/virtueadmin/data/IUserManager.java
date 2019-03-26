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
package com.ncc.savior.virtueadmin.data;

import com.ncc.savior.virtueadmin.model.VirtueUser;

/**
 * Instance of this class will manage a set of users for the system.
 */
public interface IUserManager {

	void addUser(VirtueUser user);

	VirtueUser getUser(String username);

	Iterable<VirtueUser> getAllUsers();

	/**
	 * Clears all virtues from all users. If removeAllUsers is true, it then deletes
	 * those users.
	 * 
	 * @param removeAllUsers
	 */
	void clear(boolean removeAllUsers);

	void removeUser(VirtueUser user);

	void removeUser(String usernameToRemove);

	boolean userExists(String username);

	void enableDisableUser(String username, Boolean enable);

}
