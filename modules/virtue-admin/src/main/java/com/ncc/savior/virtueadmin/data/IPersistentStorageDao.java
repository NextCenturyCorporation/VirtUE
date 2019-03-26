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

import com.ncc.savior.virtueadmin.model.VirtuePersistentStorage;

/**
 * Data Access Object for persistent storage volumes.
 * 
 *
 */
public interface IPersistentStorageDao {

	Iterable<VirtuePersistentStorage> getAllPersistentStorage();

	/**
	 * Returns value or null
	 * 
	 * @param virtueTemplateId
	 * @param userName
	 * @return
	 */
	VirtuePersistentStorage getPersistentStorage(String virtueTemplateId, String userName);

	void deletePersistentStorage(VirtuePersistentStorage ps);

	void savePersistentStorage(VirtuePersistentStorage newPs);

	/**
	 * Return all {@link VirtuePersistentStorage} for the user.
	 * 
	 * @param username
	 * @return
	 */
	Iterable<VirtuePersistentStorage> getPersistentStorageForUser(String username);

}
