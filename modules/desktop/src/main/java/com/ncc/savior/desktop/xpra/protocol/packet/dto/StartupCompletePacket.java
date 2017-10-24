package com.ncc.savior.desktop.xpra.protocol.packet.dto;

import java.util.ArrayList;
import java.util.List;

import com.ncc.savior.desktop.xpra.protocol.packet.PacketType;

/**
 * Packet is used by the server to notify the client that all windows for the
 * application have been sent. It contains no additional data.
 *
 *
 */
public class StartupCompletePacket extends Packet {

	protected StartupCompletePacket() {
		super(PacketType.STARTUP_COMPLETE);
	}

	public StartupCompletePacket(List<Object> list) {
		this();
	}

	@Override
	protected void doAddToList(ArrayList<Object> list) {
		// do nothing

	}

	@Override
	public String toString() {
		return "StartupCompletePacket [type=" + type + "]";
	}

}
