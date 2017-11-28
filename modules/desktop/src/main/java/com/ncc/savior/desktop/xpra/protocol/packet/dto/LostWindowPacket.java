package com.ncc.savior.desktop.xpra.protocol.packet.dto;

import java.util.List;

import com.ncc.savior.desktop.xpra.protocol.packet.PacketType;
import com.ncc.savior.desktop.xpra.protocol.packet.PacketUtils;

/**
 * Packet sent by server to inform the client that a window has been closed.
 *
 *
 *
 */
public class LostWindowPacket extends WindowPacket {


	protected LostWindowPacket(int windowId) {
		super(windowId, PacketType.LOST_WINDOW);
	}

	public LostWindowPacket(List<Object> list) {
		this(PacketUtils.asInt(list.get(1)));

	}

	@Override
	protected void doAddToList(List<Object> list) {
		list.add(windowId);
	}

	@Override
	public String toString() {
		return "LostWindowPacket [windowId=" + windowId + ", type=" + type + "]";
	}
}
