package com.ncc.savior.desktop.xpra.protocol.packet.dto;

import java.util.List;

import com.ncc.savior.desktop.xpra.protocol.packet.PacketType;
import com.ncc.savior.desktop.xpra.protocol.packet.PacketUtils;

/**
 * This packet informs the server that this specific window has focus.
 *
 *
 */
public class FocusPacket extends WindowPacket {

	public FocusPacket(int windowId) {
		super(windowId, PacketType.FOCUS);
	}

	public FocusPacket(List<Object> list) {
		this(PacketUtils.asInt(list.get(1)));
	}

	@Override
	protected void doAddToList(List<Object> list) {
		list.add(super.windowId);
	}

	@Override
	public String toString() {
		return "FocusPacket [windowId=" + windowId + ", type=" + type + "]";
	}

}
