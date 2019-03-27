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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.xpra.protocol.ImageEncoding;
import com.ncc.savior.desktop.xpra.protocol.packet.PacketType;
import com.ncc.savior.desktop.xpra.protocol.packet.PacketUtils;

/**
 * Packet sent by server to client containing updated pixel data for the client
 * to draw. The server often does not send the entire window unless the entire
 * window has changed.
 *
 *
 */
public class DrawPacket extends WindowPacket implements IImagePacket {
	private static final Logger logger = LoggerFactory.getLogger(DrawPacket.class);
	private int x;
	private int y;
	private int width;
	private int height;
	private ImageEncoding encoding;
	private byte[] data;
	private int sequence;
	private int rowstride;
	private Map<String, Object> meta;

	protected DrawPacket(int windowId, int x, int y, int width, int height, ImageEncoding encoding, byte[] data,
			int sequence, int rowstride) {
		super(windowId, PacketType.DRAW);
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.encoding = encoding;
		this.data = data;
		this.sequence = sequence;
		this.rowstride = rowstride;
	}

	public DrawPacket(List<Object> list) {
		super(PacketUtils.asInt(list.get(1)), PacketType.DRAW);
		list = new ArrayList<>(list);
		this.x = PacketUtils.asInt(list.get(2));
		this.y = PacketUtils.asInt(list.get(3));
		this.width = PacketUtils.asInt(list.get(4));
		this.height = PacketUtils.asInt(list.get(5));
		this.encoding = ImageEncoding.parse(PacketUtils.asString(list.get(6)));
		Object data = list.get(7);
		if (data instanceof String) {
			// logger.debug(encoding + " as string");
			this.data = ((String) data).getBytes();
		} else {
			// logger.debug(encoding + " as byte");
			this.data = (byte[]) data;
		}
		// this.data = (byte[]) list.get(7);
		this.sequence = PacketUtils.asInt(list.get(8));
		this.rowstride = PacketUtils.asInt(list.get(9));
		if (list.size() > 10) {
			this.meta = PacketUtils.asStringObjectMap(list.get(10));
			if (meta.containsKey("zlib")) {
				// Hello Packet disables zlib so we should never have this. If we ever get here,
				// something weird happened. We should warn and continue.
				logger.warn("Found DrawPacket that is claimed to be zlib encrypted!  Zlib is not implemented!");
			}
		}
	}

	@Override
	protected void doAddToList(List<Object> list) {
		list.add(windowId);
		list.add(x);
		list.add(y);
		list.add(width);
		list.add(height);
		list.add(encoding.toString());
		list.add(data);
		list.add(sequence);
		list.add(rowstride);
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
	public ImageEncoding getEncoding() {
		return encoding;
	}

	@Override
	public byte[] getData() {
		return data;
	}

	public int getSequence() {
		return sequence;
	}

	public int getRowstride() {
		return rowstride;
	}

	@Override
	public String toString() {
		return "DrawPacket [windowId=" + windowId + ", x=" + x + ", y=" + y + ", width=" + width + ", height=" + height
				+ ", encoding=" + encoding + ", dataSize=" + data.length + ", sequence=" + sequence + ", rowstride="
				+ rowstride + ", type=" + type + ", meta=" + meta + "]";
	}

}
