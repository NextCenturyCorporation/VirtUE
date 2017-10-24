package com.ncc.savior.desktop.xpra.protocol.packet;

import java.util.ArrayList;
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
	private List<IPacketHandler> handlers = new ArrayList<IPacketHandler>();

	public void addPacketHandler(IPacketHandler handler) {
		handlers.add(handler);
	}

	public void removePacketHandler(IPacketHandler handler) {
		handlers.remove(handler);
	}

	/**
	 * Packets a {@link Packet} to all the appropriate {@link IPacketHandler}s based
	 * on the types they say they can handle. The types is determined by the
	 * {@link IPacketHandler}.getType() method.
	 *
	 * @param packet
	 */
	public void handlePacket(Packet packet) {
		for (IPacketHandler handler : handlers) {
			PacketType type = packet.getType();
			Set<PacketType> validTypes = handler.getValidPacketTypes();
			if (validTypes == null || validTypes.isEmpty() || validTypes.contains(type)) {
				handler.handlePacket(packet);
			}
		}
	}

}
