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
package com.ncc.savior.desktop.xpra.protocol;

import java.util.Set;

import com.ncc.savior.desktop.xpra.protocol.packet.PacketType;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.Packet;

/**
 * Listeners for when {@link Packet}s are sent to an Xpra server or received
 * from an Xpra server. IPacketHandlers should only be called if they can accept
 * the type of packet. The type of packets they can handle is determined by the
 * {@link Set} returned by getValidPacketTypes(). If that {@link Set} is null or
 * empty, the IPacketHandler should be able to handle all packet types.
 *
 *
 */
public interface IPacketHandler {

	/**
	 * Called when a packet is sent or received.
	 *
	 * @param packet
	 */
	public void handlePacket(Packet packet);

	/**
	 * @return - returns the {@link Set} of {@link PacketType}s that this handler
	 *         can handle. Caller should ensure that this handler is not passed any
	 *         {@link Packet}s that do not match those types. A null value or an
	 *         empty {@link Set} can be used to mean ALL {@link PacketType}s.
	 */
	public Set<PacketType> getValidPacketTypes();
}
