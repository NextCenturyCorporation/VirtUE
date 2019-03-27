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

import com.ncc.savior.desktop.xpra.protocol.packet.PacketType;
import com.ncc.savior.desktop.xpra.protocol.packet.PacketUtils;

/**
 * Packet to tell the server where the mouse is.
 *
 * Modifiers can be a set of the following:
 * <ul>
 * <li>shift
 * <li>control
 * <li>alt
 * </ul>
 *
 * There are probably other modifiers
 *
 *
 */
public class MousePointerPositionPacket extends WindowPacket {

	private List<Integer> position;
	private List<String> modifiers;

	public MousePointerPositionPacket(int windowId, int x, int y, List<String> modifiers) {
		super(windowId, PacketType.POINTER_POSITION);
		position = new ArrayList<Integer>(2);
		position.add(x);
		position.add(y);
		this.modifiers = modifiers;
	}

	public MousePointerPositionPacket(int windowId, int x, int y) {
		this(windowId, x, y, new ArrayList<String>(0));
	}

	public MousePointerPositionPacket(List<Object> list) {
		this(PacketUtils.asInt(list.get(1)), PacketUtils.asInt(list.get(2)), PacketUtils.asInt(list.get(3)));
	}

	@Override
	protected void doAddToList(List<Object> list) {
		list.add(windowId);
		list.add(position);
		list.add(modifiers);
		// list.add(new ArrayList<String>());
	}

	@Override
	public String toString() {
		return "MousePointerPositionPacket [position=" + position + ", modifiers=" + modifiers + ", windowId="
				+ windowId + ", type=" + type + "]";
	}

}
