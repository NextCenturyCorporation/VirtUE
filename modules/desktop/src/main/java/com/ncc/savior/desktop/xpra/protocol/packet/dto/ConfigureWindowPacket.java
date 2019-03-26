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
 * Packet sent by Client when it has modified a window in some way.
 *
 *
 */
public class ConfigureWindowPacket extends WindowPacket {
	private int x;
	private int y;
	private int width;
	private int height;

	public ConfigureWindowPacket(int windowId, int x, int y, int width, int height) {
		super(windowId, PacketType.CONFIGURE_WINDOW);
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
    }

	public ConfigureWindowPacket(List<Object> list) {
		super(PacketUtils.asInt(list.get(1)), PacketType.CONFIGURE_WINDOW);
		this.x = PacketUtils.asInt(list.get(2));
		this.y = PacketUtils.asInt(list.get(3));
		this.width = PacketUtils.asInt(list.get(4));
		this.height = PacketUtils.asInt(list.get(5));
    }

    @Override
	protected void doAddToList(List<Object> list) {
		list.add(windowId);
		list.add(x);
		list.add(y);
		list.add(width);
		list.add(height);
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

	@Override
	public String toString() {
		return "ConfigureWindowPacket [windowId=" + windowId + ", x=" + x + ", y=" + y + ", width=" + width
				+ ", height=" + height + ", type=" + type + "]";
	}

}
