package com.ncc.savior.desktop.xpra.protocol.packet.dto;

import java.util.ArrayList;
import java.util.List;

import com.ncc.savior.desktop.xpra.protocol.packet.PacketType;
import com.ncc.savior.desktop.xpra.protocol.packet.PacketUtils;

/**
 * Packet sent by client requesting that a window be closed.
 *
 *
 */
public class CloseWindowPacket extends WindowPacket {

	public CloseWindowPacket(int windowId) {
		super(windowId, PacketType.CLOSE_WINDOW);
	}

	public CloseWindowPacket(List<Object> list) {
		this(PacketUtils.asInt(list.get(1)));

	}

	@Override
	protected void doAddToList(ArrayList<Object> list) {
		list.add(windowId);
	}

	@Override
	public String toString() {
		return "CloseWindowPacket [windowId=" + windowId + ", type=" + type + "]";
	}
}
