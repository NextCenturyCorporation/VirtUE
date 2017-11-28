package com.ncc.savior.desktop.xpra.protocol.packet;

import java.util.List;

import com.ncc.savior.desktop.xpra.protocol.packet.dto.Packet;

public class DisconnectPacket extends Packet {

	protected DisconnectPacket() {
		super(PacketType.DISCONNECT);
	}

	public DisconnectPacket(List<Object> list) {
		this();
	}

	@Override
	protected void doAddToList(List<Object> list) {
		// do nothing
	}

}
