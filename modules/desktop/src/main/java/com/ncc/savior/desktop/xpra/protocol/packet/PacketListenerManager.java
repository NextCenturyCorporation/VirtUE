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
