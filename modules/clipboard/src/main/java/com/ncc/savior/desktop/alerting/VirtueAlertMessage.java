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
package com.ncc.savior.desktop.alerting;

import com.ncc.savior.virtueadmin.model.VirtueInstance;
import com.ncc.savior.virtueadmin.model.desktop.DesktopVirtue;

/**
 * Alert message specific to a virtue. We may want to have additional ways to
 * handle these alerts.
 */
public class VirtueAlertMessage extends BaseAlertMessage {
	private String virtueId;
	private String virtueName;
	private String message;

	public VirtueAlertMessage(String title, VirtueInstance virtue, String message) {
		this(title, virtue.getId(), virtue.getName(), message);

	}

	public VirtueAlertMessage(String title, DesktopVirtue virtue, String message) {
		this(title, virtue.getId(), virtue.getName(), message);

	}

	public VirtueAlertMessage(String title, String id, String name, String message) {
		super(title);
		this.virtueId = id;
		this.virtueName = name;
		this.message = message;
	}

	@Override
	public String getPlainTextMessage() {
		return message;
	}

	public String getVirtueId() {
		return virtueId;
	}

	public String getVirtueName() {
		return virtueName;
	}
}
