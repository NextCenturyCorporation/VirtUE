package com.ncc.savior.desktop.xpra.protocol.packet.dto;

import java.util.ArrayList;
import java.util.List;

import com.ncc.savior.desktop.xpra.protocol.packet.PacketType;
import com.ncc.savior.desktop.xpra.protocol.packet.PacketUtils;

/**
 * Packet that indicates that the server has changed the cursor and the client
 * should change the cursor as well.
 *
 *
 */
public class CursorPacket extends Packet {
	private int x;
	private int y;
	private int width;
	private int height;
	private int xHotspot;
	private int yHotspot;
	private int serial;
	private byte[] bytes;
	private String name;
	private boolean empty;

	protected CursorPacket() {
		super(PacketType.CURSOR);
	}

	public CursorPacket(List<Object> list) {
		super(PacketType.CURSOR);
		Object first = list.get(1);
		if (first instanceof String) {
			if (((String) first).isEmpty()) {
				this.empty = true;
				return;
			} else {
				this.bytes = (byte[]) first;
			}
		}
		this.x = PacketUtils.asInt(first);
		this.y = PacketUtils.asInt(list, 2);
		this.width = PacketUtils.asInt(list, 3);
		this.height = PacketUtils.asInt(list, 4);
		this.xHotspot = PacketUtils.asInt(list, 5);
		this.yHotspot = PacketUtils.asInt(list, 6);
		this.serial = PacketUtils.asInt(list, 7);
		this.bytes = (byte[]) list.get(8);
		if (list.size() > 9) {
			this.name = PacketUtils.asString(list.get(9));
		}
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public int getxHotspot() {
		return xHotspot;
	}

	public int getyHotspot() {
		return yHotspot;
	}

	public int getSerial() {
		return serial;
	}

	public byte[] getBytes() {
		return bytes;
	}

	public String getName() {
		return name;
	}

	public boolean isEmpty() {
		return empty;
	}

	@Override
	protected void doAddToList(ArrayList<Object> list) {
		throw new RuntimeException("Not implemented");
	}

}
