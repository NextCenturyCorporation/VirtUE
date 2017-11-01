package com.ncc.savior.desktop.xpra.protocol.packet;

import java.util.HashSet;
import java.util.Set;

import com.ncc.savior.desktop.xpra.protocol.IPacketHandler;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.Packet;

public abstract class BasePacketHandler implements IPacketHandler {
	private Set<PacketType> types;

	protected BasePacketHandler() {
		// valid for all types
		types = null;
	}

	protected BasePacketHandler(PacketType type) {
		types = new HashSet<PacketType>();
		types.add(type);
	}

	@Override
	public abstract void handlePacket(Packet packet);

	@Override
	public Set<PacketType> getValidPacketTypes() {
		return types;
	}

}
