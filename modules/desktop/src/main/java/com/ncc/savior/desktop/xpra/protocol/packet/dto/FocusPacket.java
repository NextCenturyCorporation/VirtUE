package com.ncc.savior.desktop.xpra.protocol.packet.dto;

import java.util.ArrayList;

import com.ncc.savior.desktop.xpra.protocol.packet.PacketType;

/**
 * This packet informs the server that this specific window has focus.
 *
 *
 */
public class FocusPacket extends WindowPacket {

	public FocusPacket(int windowId) {
		super(windowId, PacketType.FOCUS);
	}

	@Override
	protected void doAddToList(ArrayList<Object> list) {
		list.add(super.windowId);
	}

}
