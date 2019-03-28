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
 * Packet to inform the server that a mouse button state has changed. Modifiers
 * can be a set of the following:
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
public class MouseButtonActionPacket extends WindowPacket {
	private List<Integer> position;
	private List<String> modifiers;
	private boolean pressed;
	private int button;

	public MouseButtonActionPacket(int windowId, int button, boolean pressed, int x, int y, List<String> modifiers) {
		super(windowId, PacketType.BUTTON_ACTION);
		this.button = button;
		this.pressed = pressed;
		this.position = new ArrayList<Integer>(2);
		this.position.add(x);
		this.position.add(y);
		this.modifiers = modifiers;
	}

	public MouseButtonActionPacket(int windowId, int button, boolean pressed, int x, int y) {
		this(windowId, button, pressed, x, y, new ArrayList<String>(2));
	}

	public MouseButtonActionPacket(List<Object> list) {
		this(PacketUtils.asInt(list.get(1)), PacketUtils.asInt(list.get(2)), PacketUtils.asBoolean(list.get(3)),
				PacketUtils.asInt(list.get(4)), PacketUtils.asInt(list.get(5)));
	}

	@Override
	protected void doAddToList(List<Object> list) {
		list.add(windowId);
		list.add(button);
		list.add(pressed);
		list.add(position);
		list.add(modifiers);
	}

	@Override
	public String toString() {
		return "MouseButtonAction [position=" + position + ", modifiers=" + modifiers + ", windowId=" + windowId
				+ ", pressed=" + pressed + ", button=" + button + ", type=" + type + "]";
	}
}
