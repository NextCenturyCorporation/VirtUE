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

import java.io.Serializable;
import java.util.Date;

/**
 * Base class for a message between clipboard clients and the hub. This is the
 * basis of communication between them.
 *
 *
 */
public class BaseClipboardMessage implements IClipboardMessage, Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	protected Date sendTime;
	protected String messageSourceId;

	// For Jackson (de)serialization
	protected BaseClipboardMessage() {
		this(null);
	}

	public BaseClipboardMessage(String messageSourceId) {
		this.sendTime = new Date();
		this.messageSourceId = messageSourceId;
	}

	@Override
	public Date getSendTime() {
		return sendTime;
	}

	public void setSendTime(Date sendTime) {
		this.sendTime = sendTime;
	}

	@Override
	public String getSourceId() {
		return messageSourceId;
	}

	public void setSourceId(String messageSourceId) {
		this.messageSourceId = messageSourceId;
	}

}
