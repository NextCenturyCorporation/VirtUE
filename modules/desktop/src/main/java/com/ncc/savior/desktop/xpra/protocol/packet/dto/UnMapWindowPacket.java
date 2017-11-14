package com.ncc.savior.desktop.xpra.protocol.packet.dto;

import java.util.List;

import com.ncc.savior.desktop.xpra.protocol.packet.PacketType;
import com.ncc.savior.desktop.xpra.protocol.packet.PacketUtils;

/**
 * Send when a window is minimized.
 *
 *
 */
public class UnMapWindowPacket extends WindowPacket {

	public UnMapWindowPacket(int windowId) {
		super(windowId, PacketType.UNMAP_WINDOW);
	}

	public UnMapWindowPacket(List<Object> list) {
		this(PacketUtils.asInt(list, 1));
	}

	@Override
	protected void doAddToList(List<Object> list) {
		list.add(windowId);
	}

}
