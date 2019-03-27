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

import com.ncc.savior.desktop.clipboard.data.ClipboardData;

/**
 * Message containing {@link ClipboardData}. This should be sent in response to
 * a {@link ClipboardDataRequestMessage}.
 *
 *
 */
public class ClipboardDataMessage extends BaseClipboardMessage implements Serializable {

	private static final long serialVersionUID = 1L;
	private ClipboardData data;
	private String requestId;
	private String destinationId;

	// For Jackson (de)serialization
	protected ClipboardDataMessage() {
		this(null, null, null, null);
	}

	public ClipboardDataMessage(String ownerId, ClipboardData data, String requestId, String destinationId) {
		super(ownerId);
		this.data = data;
		this.requestId = requestId;
		this.destinationId = destinationId;
	}

	public ClipboardData getData() {
		return data;
	}

	public String getRequestId() {
		return requestId;
	}

	public String getDestinationId() {
		return destinationId;
	}

	@Override
	public String toString() {
		return "ClipboardDataMessage [data=" + data + ", requestId=" + requestId + ", destinationId=" + destinationId
				+ ", sendTime=" + sendTime + ", messageSourceId=" + messageSourceId + "]";
	}

	// For Jackson (de)serialization
	protected void setData(ClipboardData data) {
		this.data = data;
	}

	// For Jackson (de)serialization
	protected void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	// For Jackson (de)serialization
	protected void setDestinationId(String destinationId) {
		this.destinationId = destinationId;
	}

}
