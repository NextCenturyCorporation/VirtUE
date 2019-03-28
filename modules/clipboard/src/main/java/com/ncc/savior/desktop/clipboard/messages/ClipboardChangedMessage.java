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
import java.util.Set;

import com.ncc.savior.desktop.clipboard.ClipboardFormat;

/**
 * Message that indicates that the clipboard has changed on the source machine
 *
 *
 */
public class ClipboardChangedMessage extends BaseClipboardMessage implements Serializable {
	private static final long serialVersionUID = 1L;
	private Set<ClipboardFormat> formats;

	// For Jackson (de)serialization
	protected ClipboardChangedMessage() {
		this(null, null);
	}

	public ClipboardChangedMessage(String sourceId, Set<ClipboardFormat> formats) {
		super(sourceId);
		this.formats = formats;
	}

	/**
	 * Returns the available formats on the clipboard.
	 *
	 * @return
	 */
	public Set<ClipboardFormat> getFormats() {
		return formats;
	}

	@Override
	public String toString() {
		return "ClipboardChangedMessage [formats=" + formats + ", sendTime=" + sendTime + ", messageSourceId="
				+ messageSourceId + "]";
	}

	// For Jackson (de)serialization
	protected void setFormats(Set<ClipboardFormat> formats) {
		this.formats = formats;
	}
}
