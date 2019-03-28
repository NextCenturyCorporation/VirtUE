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

import com.ncc.savior.desktop.xpra.protocol.packet.PacketType;
import com.ncc.savior.desktop.xpra.protocol.packet.PacketUtils;

/**
 * Sent by client to request a change in the connections compression level. If
 * the server can honor the request, it should send the same packet back to the
 * client.
 *
 *
 */
public class SetDeflatePacket extends Packet {

	private int compression;

	public SetDeflatePacket(int compression) {
		super(PacketType.SET_DEFLATE);
		this.compression = compression;
	}

	public SetDeflatePacket(List<Object> list) {
		this(PacketUtils.asInt(list.get(1)));
	}

	@Override
	protected void doAddToList(List<Object> list) {
		list.add(compression);
	}

	@Override
	public String toString() {
		return "SetDeflatePacket [compression=" + compression + ", type=" + type + "]";
	}

}
