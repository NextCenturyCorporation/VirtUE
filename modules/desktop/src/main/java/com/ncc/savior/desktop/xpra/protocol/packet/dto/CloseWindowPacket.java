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
public class CloseWindowPacket extends Packet {
	private int windowId;

	protected CloseWindowPacket(int windowId) {
		super(PacketType.CLOSE_WINDOW);
		this.windowId = windowId;

	}

	public CloseWindowPacket(List<Object> list) {
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
		return "CloseWindowPacket [windowId=" + windowId + ", type=" + type + "]";
	}
}
