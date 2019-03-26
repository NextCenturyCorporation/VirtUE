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

import com.ncc.savior.desktop.clipboard.ClipboardFormat;

/**
 * Message that requests the current active clipboard data for the given format.
 *
 */
public class ClipboardDataRequestMessage extends BaseClipboardMessage implements Serializable {

	private static final long serialVersionUID = 1L;
	private ClipboardFormat format;
	private String requestId;

	// For Jackson (de)serialization
	protected ClipboardDataRequestMessage() {
		this(null, null, null);
	}

	public ClipboardDataRequestMessage(String ownerId, ClipboardFormat format, String requestId) {
		super(ownerId);
		this.format = format;
		this.requestId = requestId;
	}

	public ClipboardFormat getFormat() {
		return format;
	}

	public String getRequestId() {
		return requestId;
	}

	@Override
	public String toString() {
		return "ClipboardDataRequestMessage [format=" + format + ", requestId=" + requestId + ", sendTime=" + sendTime
				+ ", messageSourceId=" + messageSourceId + "]";
	}

	// For Jackson (de)serialization
	protected void setFormat(ClipboardFormat format) {
		this.format = format;
	}

	// For Jackson (de)serialization
	protected void setRequestId(String requestId) {
		this.requestId = requestId;
	}

}
