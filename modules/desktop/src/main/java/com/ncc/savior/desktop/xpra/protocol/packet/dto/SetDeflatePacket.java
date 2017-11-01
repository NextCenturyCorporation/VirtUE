package com.ncc.savior.desktop.xpra.protocol.packet.dto;

import java.util.ArrayList;
import java.util.List;

import com.ncc.savior.desktop.xpra.protocol.packet.PacketType;
import com.ncc.savior.desktop.xpra.protocol.packet.PacketUtils;

/**
 * Sent by client to request a change in the connections compression level. If
 * the server can honor the request, it should send the same packet back to the
 * client.
 *
 *
 */
public class SetDeflatePacket extends Packet {

	private int compression;

	public SetDeflatePacket(int compression) {
		super(PacketType.SET_DEFLATE);
		this.compression = compression;
	}

	public SetDeflatePacket(List<Object> list) {
		this(PacketUtils.asInt(list.get(1)));
	}

	@Override
	protected void doAddToList(ArrayList<Object> list) {
		list.add(compression);
	}

	@Override
	public String toString() {
		return "SetDeflatePacket [compression=" + compression + ", type=" + type + "]";
	}

}
