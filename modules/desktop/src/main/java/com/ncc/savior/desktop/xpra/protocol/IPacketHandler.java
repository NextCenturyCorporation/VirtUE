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
