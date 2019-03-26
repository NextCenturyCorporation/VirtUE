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

/**
 * Should be returned when a {@link PingPacket} is received. Receiver should
 * send the timestamp from the {@link PingPacket} back in this packet. The other
 * data in this packet does not seem to be used.
 *
 *
 */
public class PingEchoPacket extends Packet {

	private long timestamp;
	// Cannot find a use for the following, but they are part of the packet.
	private int l1;
	private int l2;
	private int l3;
	private int other;

	public PingEchoPacket(long timestamp) {
		super(PacketType.PING_ECHO);
		this.timestamp = timestamp;
	}

	public PingEchoPacket(List<Object> list) {
		this((long) list.get(1));
		if (list.size() > 2) {
			l1 = ((Number) list.get(2)).intValue();
		}
		if (list.size() > 3) {
			l2 = ((Number) list.get(3)).intValue();
		}
		if (list.size() > 4) {
			l3 = ((Number) list.get(4)).intValue();
		}
		if (list.size() > 5) {
			other = ((Number) list.get(5)).intValue();
		}

	}

	@Override
	protected void doAddToList(List<Object> list) {
		list.add(timestamp);
		list.add(l1);
		list.add(l2);
		list.add(l3);
		list.add(other);
	}

	public long getTimestamp() {
		return timestamp;
	}

	public int getL1() {
		return l1;
	}

	public int getL2() {
		return l2;
	}

	public int getL3() {
		return l3;
	}

	public int getOther() {
		return other;
	}

	@Override
	public String toString() {
		return "PingEchoPacket [timestamp=" + timestamp + ", l1=" + l1 + ", l2=" + l2 + ", l3=" + l3 + ", other="
				+ other + ", type=" + type + "]";
	}

}
