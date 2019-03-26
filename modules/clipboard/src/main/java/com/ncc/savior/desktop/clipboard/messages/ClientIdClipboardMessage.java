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
package com.ncc.savior.desktop.clipboard.messages;

/**
 * Message to inform the client of their ID. The client should wait for this on
 * connection and then use the ID in all messages it sends.
 *
 *
 */
public class ClientIdClipboardMessage extends BaseClipboardMessage implements IClipboardMessage {

	private static final long serialVersionUID = 1L;
	private String newId;

	// For Jackson (de)serialization
	protected ClientIdClipboardMessage() {
		this(null, null);
	}

	public ClientIdClipboardMessage(String messageSourceId, String newId) {
		super(messageSourceId);
		this.newId = newId;
	}

	public String getNewId() {
		return newId;
	}

	// For Jackson (de)serialization
	protected void setNewId(String newId) {
		this.newId = newId;
	}

	@Override
	public String toString() {
		return "ClientIdClipboardMessage [newId=" + newId + ", sendTime=" + sendTime + ", messageSourceId="
				+ messageSourceId + "]";
	}
}
