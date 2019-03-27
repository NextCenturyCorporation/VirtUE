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
package com.ncc.savior.virtueadmin.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description="Describes the status of the Virtue.")
public enum VirtueState {
	
	ERROR(10), RUNNING(20), RESUMING(30), LAUNCHING(40), CREATING(50), PAUSED(60), PAUSING(70), STOPPING(80), 
	STOPPED(90), DELETING(100), DELETED(110), UNPROVISIONED(120);
	
	VirtueState(int value) {
		this.value = value;
	}

	private final int value;

	public int getValue() {
		return value;
	}
}
