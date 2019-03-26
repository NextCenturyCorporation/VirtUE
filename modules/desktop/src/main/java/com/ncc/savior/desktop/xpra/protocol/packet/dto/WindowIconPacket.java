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

import java.util.Arrays;
import java.util.List;

import com.ncc.savior.desktop.xpra.protocol.ImageEncoding;
import com.ncc.savior.desktop.xpra.protocol.packet.PacketType;
import com.ncc.savior.desktop.xpra.protocol.packet.PacketUtils;

/**
 * Packet that indicate the icon for the window has changed. This icon usually
 * appears in the top left of the window as well as any taskbar from the OS.
 *
 *
 */
public class WindowIconPacket extends WindowPacket implements IImagePacket {

	private int width;
	private int height;
	private ImageEncoding encoding;
	private byte[] data;

	protected WindowIconPacket(int windowId, int width, int height, ImageEncoding encoding, byte[] data) {
		super(windowId, PacketType.WINDOW_ICON);
		this.width = width;
		this.height = height;
		this.encoding = encoding;
		this.data = data;
	}

	public WindowIconPacket(List<Object> list) {
		super(PacketUtils.asInt(list.get(1)), PacketType.WINDOW_ICON);
		this.width = PacketUtils.asInt(list.get(2));
		this.height = PacketUtils.asInt(list.get(3));
		this.encoding = ImageEncoding.parse(PacketUtils.asString(list.get(4)));
		Object o = list.get(5);
		if (o instanceof String) {
			this.data = ((String) o).getBytes();
		} else {
			this.data = (byte[]) o;
		}
	}

	@Override
	protected void doAddToList(List<Object> list) {
		list.add(windowId);
		list.add(width);
		list.add(height);
		list.add(encoding.getCode());
		list.add(data);
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	@Override
	public ImageEncoding getEncoding() {
		return encoding;
	}

	@Override
	public byte[] getData() {
		return data;
	}

	@Override
	public String toString() {
		return "WindowIconPacket [width=" + width + ", height=" + height + ", encoding=" + encoding + ", data="
				+ Arrays.toString(data) + "]";
	}
}
