package com.ncc.savior.desktop.xpra.protocol.packet.dto;

import java.util.ArrayList;
import java.util.List;

import com.ncc.savior.desktop.xpra.protocol.packet.PacketType;
import com.ncc.savior.desktop.xpra.protocol.packet.PacketUtils;

/**
 * Packet sent by server to inform the client that a window has been moved or
 * resized.
 *
 *
 *
 */
public class WindowMoveResizePacket extends WindowPacket {

	private int x;
	private int y;
	private int width;
	private int height;

	protected WindowMoveResizePacket(int windowId, int x, int y, int width, int height) {
		super(windowId, PacketType.LOST_WINDOW);
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	public WindowMoveResizePacket(List<Object> list) {
		this(PacketUtils.asInt(list.get(1)), PacketUtils.asInt(list.get(2)), PacketUtils.asInt(list.get(3)),
				PacketUtils.asInt(list.get(4)), PacketUtils.asInt(list.get(5)));
	}

	@Override
	protected void doAddToList(ArrayList<Object> list) {
		list.add(windowId);
		list.add(x);
		list.add(y);
		list.add(width);
		list.add(height);
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

	@Override
	public String toString() {
		return "WindowMoveResizePacket [windowId=" + windowId + ", x=" + x + ", y=" + y + ", width=" + width
				+ ", height=" + height + ", type=" + type + "]";
	}

}
