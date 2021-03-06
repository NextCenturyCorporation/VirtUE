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
 * Packet that indicates that the server has changed the cursor and the client
 * should change the cursor as well.
 *
 *
 */
public class CursorPacket extends Packet {
	private int x;
	private int y;
	private int width;
	private int height;
	private int xHotspot;
	private int yHotspot;
	private int serial;
	private byte[] bytes;
	private String name;
	private boolean empty;
	private String format;

	protected CursorPacket() {
		super(PacketType.CURSOR);
	}

	public CursorPacket(List<Object> list) {
		super(PacketType.CURSOR);
		Object first = list.get(1);
		int index = 1;
		// for some reason first can be a string with type, an empty string, or x
		if (first instanceof String) {
			if (((String) first).isEmpty()) {
				this.empty = true;
				return;
			} else {
				this.format = (String) first;
				index = 2;
			}
		}
		if (first instanceof byte[]) {
			if (((byte[]) first).length == 0) {
				this.empty = true;
				return;
			} else {
				this.bytes = (byte[]) first;
			}
		}
		this.x = PacketUtils.asInt(list, index);
		this.y = PacketUtils.asInt(list, ++index);
		this.width = PacketUtils.asInt(list, ++index);
		this.height = PacketUtils.asInt(list, ++index);
		this.xHotspot = PacketUtils.asInt(list, ++index);
		this.yHotspot = PacketUtils.asInt(list, ++index);
		this.serial = PacketUtils.asInt(list, ++index);
		Object potentialName = list.get(index + 2);
		if (potentialName instanceof byte[]) {
			this.name = new String((byte[]) potentialName);
			this.bytes = (byte[]) list.get(index + 1);
			index += 2;
		} else {
			this.name = new String((byte[]) list.get(++index));
		}
		// System.out.println("cursor packet" +
		// PacketUtils.convertByteArraysToStrings(list));
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public int getxHotspot() {
		return xHotspot;
	}

	public int getyHotspot() {
		return yHotspot;
	}

	public int getSerial() {
		return serial;
	}

	public byte[] getBytes() {
		return bytes;
	}

	public String getName() {
		return name;
	}

	public boolean isEmpty() {
		return empty;
	}

	@Override
	protected void doAddToList(List<Object> list) {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public String toString() {
		return "CursorPacket [x=" + x + ", y=" + y + ", width=" + width + ", height=" + height + ", xHotspot="
				+ xHotspot + ", yHotspot=" + yHotspot + ", serial=" + serial + ", bytes="
				+ (bytes == null ? 0 : bytes.length) + ", name=" + name + ", empty=" + empty + ", type=" + type + "]";
	}

}
