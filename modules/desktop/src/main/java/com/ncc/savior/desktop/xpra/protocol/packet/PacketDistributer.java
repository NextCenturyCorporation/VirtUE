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

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.xpra.protocol.IPacketHandler;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.Packet;

/**
 * A specialized {@link IPacketHandler} used internally by the Xpra client such
 * that it handles all packets in a default way.
 *
 *
 */
public class PacketDistributer extends BasePacketHandler {
	private static final Logger logger = LoggerFactory.getLogger(PacketDistributer.class);

	private IPacketHandler otherHandler;
	private Map<PacketType, IPacketHandler> handlers;

	public PacketDistributer() {
		super();
		handlers = new HashMap<PacketType, IPacketHandler>();
	}

	public void setOtherHandler(IPacketHandler handler) {
		this.otherHandler = handler;
	}

	public <T extends Packet> void addPacketHandler(PacketType type, IPacketHandler handler) {
		handlers.put(type, handler);
	}

	@Override
	public void handlePacket(Packet packet) {
		IPacketHandler handler = handlers.get(packet.getType());
		if (handler == null) {
			if (otherHandler == null) {
				return;
			} else {
				handler = otherHandler;
			}
		}
		if (logger.isTraceEnabled()) {
			logger.trace(
					"Received packet=" + packet + " of type=" + packet.getType() + "and passing to handler="
							+ (handler == null ? handler : "OtherHandler"));
		}
		handler.handlePacket(packet);
	}
}
