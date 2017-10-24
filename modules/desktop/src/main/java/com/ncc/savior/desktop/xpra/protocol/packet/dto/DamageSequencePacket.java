package com.ncc.savior.desktop.xpra.protocol.packet.dto;

import java.util.ArrayList;

import com.ncc.savior.desktop.xpra.protocol.packet.PacketType;

/**
 * Packet that client should send once it has received and drawn the data from a
 * {@link DrawPacket}. Server may use information to estimate connection
 * performance.
 *
 *
 */
public class DamageSequencePacket extends Packet {
	private int sequence;
	private int windowId;
	private int width;
	private int height;
	private long frametime;

	public DamageSequencePacket(int sequence, int windowId, int width, int height, long frametime) {
		super(PacketType.DAMAGE_SEQUENCE);
		this.sequence = sequence;
		this.windowId = windowId;
		this.width = width;
		this.height = height;
		this.frametime = frametime;
	}

	public DamageSequencePacket(DrawPacket packet) {
		this(packet.getSequence(), packet.getWindowId(), packet.getWidth(), packet.getHeight(), packet.getSequence());
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
