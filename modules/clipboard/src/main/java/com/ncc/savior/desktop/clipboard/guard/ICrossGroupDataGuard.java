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
package com.ncc.savior.desktop.clipboard.guard;

import java.util.Map;

/**
 * Determines whether data can flow between groupIds (VirtueId's in our specific
 * scenario). Group IDs are just a generic ID set for whatever group of machines
 * the user needs. For each combination of ID, a data guard can set a different
 * protection mechanism between them.
 *
 *
 */
public interface ICrossGroupDataGuard {

	/**
	 * Returns true if data should be allowed to be transfered from
	 * dataSourceGroupId and dataDestinationGroupId.
	 *
	 * @param dataSourceGroupId
	 * @param dataDestinationGroupId
	 * @return
	 */
	boolean allowDataTransfer(String dataSourceGroupId, String dataDestinationGroupId);

	/**
	 * do any initialization that the data guard requires after the system has been
	 * setup. This could be tasks such as initializing caches.
	 */
	public void init();

	void setGroupIdToDisplayNameMap(Map<String, String> groupIdToDisplayName);

}
