package com.ncc.savior.desktop.xpra.protocol.packet.dto;

import java.util.ArrayList;
import java.util.List;

import com.ncc.savior.desktop.xpra.protocol.packet.PacketType;

/**
 * Can be sent by either client or server, but not required to be sent by
 * client. When received, receiver must respond back with the
 * {@link PingEchoPacket}. This packet contains a sender-specific-format
 * timestamp which should be returned by the receiver.
 *
 *
 */
public class PingPacket extends Packet {

	private long time;

	public PingPacket(long time) {
		super(PacketType.PING);
		this.time = time;
	}

	public PingPacket(List<Object> list) {
		this((long) list.get(1));

	}

	@Override
	protected void doAddToList(ArrayList<Object> list) {
		list.add(time);
	}

	public long getTime() {
		return time;
	}

	@Override
	public String toString() {
		return "PingPacket [time=" + time + ", type=" + type + "]";
	}

}
