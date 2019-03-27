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
package com.ncc.savior.desktop.xpra.protocol.packet.dto;

import java.util.List;
import java.util.Map;

import com.ncc.savior.desktop.xpra.protocol.packet.PacketType;
import com.ncc.savior.desktop.xpra.protocol.packet.PacketUtils;

/**
 * Packet representing an update to the metadata for a window. See
 * {@link WindowMetadata} for details on what can be included.
 *
 *
 */
public class WindowMetadataPacket extends WindowPacket {

	private Map<String, Object> metadataRaw;
	private WindowMetadata metadata;

	public WindowMetadataPacket(int windowId, Map<String, Object> metadata) {
		super(windowId, PacketType.WINDOW_METADATA);
		this.metadataRaw = metadata;
		this.metadata = new WindowMetadata(metadataRaw);
	}

	public WindowMetadataPacket(List<Object> list) {
		this(PacketUtils.asInt(list.get(1)), PacketUtils.asStringObjectMap(list, 2));
	}

	@Override
	protected void doAddToList(List<Object> list) {
		list.add(windowId);
		list.add(metadataRaw);
	}

	public Map<String, Object> getMetadataRaw() {
		return metadataRaw;
	}

	public WindowMetadata getMetadata() {
		return metadata;
	}

	@Override
	public String toString() {
		return "WindowMetadataPacket [metadataRaw=" + metadataRaw + ", metadata=" + metadata + ", windowId=" + windowId
				+ ", type=" + type + "]";
	}
}
