package com.ncc.savior.desktop.xpra.protocol.packet.dto;

import java.util.ArrayList;
import java.util.List;

import com.ncc.savior.desktop.xpra.protocol.packet.PacketType;
import com.ncc.savior.desktop.xpra.protocol.packet.PacketUtils;

/**
 * Packet that client should send once it has received and drawn the data from a
 * {@link DrawPacket}. Server may use information to estimate connection
 * performance.
 *
 *
 */
public class DamageSequencePacket extends WindowPacket {
	private int sequence;
	private int width;
	private int height;
	private long frametime;

	public DamageSequencePacket(int sequence, int windowId, int width, int height, long frametime) {
		super(windowId, PacketType.DAMAGE_SEQUENCE);
		this.sequence = sequence;
		this.windowId = windowId;
		this.width = width;
		this.height = height;
		this.frametime = frametime;
	}

	public DamageSequencePacket(DrawPacket packet) {
		this(packet.getSequence(), packet.getWindowId(), packet.getWidth(), packet.getHeight(), packet.getSequence());
	}

	public DamageSequencePacket(List<Object> list) {
		super(PacketUtils.asInt(list, 2), PacketType.DAMAGE_SEQUENCE);
		this.sequence = PacketUtils.asInt(list, 1);
		this.width = PacketUtils.asInt(list, 3);
		this.height = PacketUtils.asInt(list, 4);
		this.frametime = PacketUtils.asLong(list, 5);
	}

	@Override
	protected void doAddToList(ArrayList<Object> list) {
		list.add(sequence);
		list.add(windowId);
		list.add(width);
		list.add(height);
		list.add(frametime);
	}

	@Override
	public String toString() {
		return "DamageSequencePacket [sequence=" + sequence + ", windowId=" + windowId + ", width=" + width
				+ ", height=" + height + ", frametime=" + frametime + ", type=" + type + "]";
	}

}
