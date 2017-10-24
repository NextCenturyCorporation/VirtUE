package com.ncc.savior.desktop.xpra.protocol.packet.dto;

import java.util.ArrayList;
import java.util.List;

import com.ncc.savior.desktop.xpra.protocol.packet.PacketType;
import com.ncc.savior.desktop.xpra.protocol.packet.PacketUtils;

/**
 * Packet sent by server to inform the client that a window has been closed.
 *
 *
 *
 */
public class LostWindowPacket extends Packet {

	private int windowId;

	protected LostWindowPacket(int windowId) {
		super(PacketType.LOST_WINDOW);
		this.windowId = windowId;
	}

	public LostWindowPacket(List<Object> list) {
		this(PacketUtils.asInt(list.get(1)));

	}

	@Override
	protected void doAddToList(ArrayList<Object> list) {
		list.add(windowId);
	}

	public int getWindowId() {
		return windowId;
	}

	@Override
	public String toString() {
		return "LostWindowPacket [windowId=" + windowId + ", type=" + type + "]";
	}
}
