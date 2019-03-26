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
package com.ncc.savior.virtueadmin.data.jpa;

import java.util.List;

import com.ncc.savior.virtueadmin.model.ClipboardPermission;
import com.ncc.savior.virtueadmin.model.ClipboardPermissionOption;

/**
 * Data Access Object interface that handles setting and retrieving permissions
 * for different services. The first service will be the clipboard dataguard.
 * 
 *
 */
public interface IPermissionDao {

	/**
	 * Retrieves the permission stored (if any) for a given source and destination.
	 * Will return null if no permission found.
	 * 
	 * @param sourceGroupId
	 * @param destinationGroupId
	 * @return
	 */
	ClipboardPermission getClipboardPermission(String sourceGroupId, String destinationGroupId);

	/**
	 * Set a single permission
	 * 
	 * @param sourceId
	 * @param destinationId
	 * @param option
	 */
	void setClipboardPermission(String sourceId, String destinationId, ClipboardPermissionOption option);

	/**
	 * Retrieve all permissions stored in the database with a given source ID.
	 * 
	 * @param sourceId
	 * @return
	 */
	List<ClipboardPermission> getClipboardPermissionForSource(String sourceId);

	/**
	 * Retrieve all permissions stored in the database with a given destination ID.
	 * 
	 * @param sourceId
	 * @return
	 */
	List<ClipboardPermission> getClipboardPermissionForDestination(String destinationId);

	/**
	 * Retrieve all permissions stored in the database.
	 * 
	 * @param sourceId
	 * @return
	 */
	Iterable<ClipboardPermission> getAllClipboardPermissions();

	/**
	 * Deletes a permission from the database
	 */
	void clearPermission(String sourceId, String destId);

}