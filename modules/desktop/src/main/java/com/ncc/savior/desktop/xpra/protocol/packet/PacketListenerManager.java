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
package com.ncc.savior.desktop.xpra.protocol.packet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.ncc.savior.desktop.xpra.protocol.IPacketHandler;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.Packet;

/**
 * Convenience class to manage all the {@link IPacketHandler}s. Classes that
 * contain a PacketListenerManager should also have an AddPacketHandler and
 * removePacketHandler method that maps directly to this.
 *
 */
public class PacketListenerManager {
	private List<IPacketHandler> handlers;
	private List<IPacketHandler> handlersToBeAdded; // needed to avoid concurrentmodificationexcption

	public PacketListenerManager() {
		handlers = new ArrayList<IPacketHandler>();
		handlersToBeAdded = new ArrayList<IPacketHandler>();
	}

	public synchronized void addPacketHandler(IPacketHandler handler) {
		handlersToBeAdded.add(handler);
	}

	public synchronized void removePacketHandler(IPacketHandler handler) {
		handlers.remove(handler);
	}

	/**
	 * Packets a {@link Packet} to all the appropriate {@link IPacketHandler}s based
	 * on the types they say they can handle. The types is determined by the
	 * {@link IPacketHandler}.getType() method.
	 *
	 * @param packet
	 */
	public synchronized void handlePacket(Packet packet) {
		if (!handlersToBeAdded.isEmpty()) {
			handlers.addAll(handlersToBeAdded);
			handlersToBeAdded.clear();
		}
		Iterator<IPacketHandler> itr = handlers.iterator();
		while (itr.hasNext()) {
			IPacketHandler handler = itr.next();
			PacketType type = packet.getType();
			Set<PacketType> validTypes = handler.getValidPacketTypes();
			if (validTypes == null || validTypes.isEmpty() || validTypes.contains(type)) {
				handler.handlePacket(packet);
			}
		}
	}

}
