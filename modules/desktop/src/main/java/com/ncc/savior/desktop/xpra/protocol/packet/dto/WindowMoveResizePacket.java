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
 * Packet sent by server to inform the client that a window has been moved or
 * resized.
 *
 *
 *
 */
public class WindowMoveResizePacket extends WindowPacket {

	private int x;
	private int y;
	private int width;
	private int height;
	private int resizeCounter;

	protected WindowMoveResizePacket(int windowId, int x, int y, int width, int height, int resizeCounter) {
		super(windowId, PacketType.WINDOW_MOVE_RESIZE);
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.resizeCounter = resizeCounter;
	}

	public WindowMoveResizePacket(List<Object> list) {
		this(PacketUtils.asInt(list.get(1)), PacketUtils.asInt(list.get(2)), PacketUtils.asInt(list.get(3)),
				PacketUtils.asInt(list.get(4)), PacketUtils.asInt(list.get(5)), PacketUtils.asInt(list, 6));
	}

	@Override
	protected void doAddToList(List<Object> list) {
		list.add(windowId);
		list.add(x);
		list.add(y);
		list.add(width);
		list.add(height);
		list.add(resizeCounter);
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

	public int getResizeCounter() {
		return resizeCounter;
	}

	@Override
	public String toString() {
		return "WindowMoveResizePacket [x=" + x + ", y=" + y + ", width=" + width + ", height=" + height
				+ ", resizeCounter=" + resizeCounter + ", windowId=" + windowId + ", type=" + type + "]";
	}

}
