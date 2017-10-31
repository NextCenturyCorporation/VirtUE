package com.ncc.savior.desktop.xpra.protocol.packet.dto;

import java.util.ArrayList;
import java.util.List;

import com.ncc.savior.desktop.xpra.protocol.packet.PacketType;
import com.ncc.savior.desktop.xpra.protocol.packet.PacketUtils;

/**
 *
 *
 * Note: this class could be a window packet because it has a window ID
 */
public class RaiseWindowPacket extends WindowPacket {

	protected RaiseWindowPacket(int windowId) {
		super(windowId, PacketType.RAISE_WINDOW);
	}

	public RaiseWindowPacket(List<Object> list) {
		this(PacketUtils.asInt(list.get(1)));
	}

	@Override
	protected void doAddToList(ArrayList<Object> list) {
		list.add(windowId);
	}
}
