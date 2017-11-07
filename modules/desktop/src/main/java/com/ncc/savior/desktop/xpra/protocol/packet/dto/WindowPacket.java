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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + windowId;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		WindowPacket other = (WindowPacket) obj;
		if (windowId != other.windowId) {
			return false;
		}
		return true;
	}

}
