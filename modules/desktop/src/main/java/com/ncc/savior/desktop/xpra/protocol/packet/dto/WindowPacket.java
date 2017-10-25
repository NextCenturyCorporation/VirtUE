package com.ncc.savior.desktop.xpra.protocol.packet.dto;

import com.ncc.savior.desktop.xpra.protocol.packet.PacketType;

/**
 * Base class to handle {@link Packet}s that are for a specific window and thus
 * have a windowID.
 *
 *
 */
public abstract class WindowPacket extends Packet {

	protected int windowId;

	public WindowPacket(int windowId, PacketType type) {
		super(type);
		this.windowId = windowId;
	}

	public int getWindowId() {
		return windowId;
	}

}
