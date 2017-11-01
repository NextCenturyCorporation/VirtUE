package com.ncc.savior.desktop.xpra.protocol.packet.dto;

import java.util.ArrayList;
import java.util.List;

import com.ncc.savior.desktop.xpra.protocol.packet.PacketType;
import com.ncc.savior.desktop.xpra.protocol.packet.PacketUtils;

public class MapWindowPacket extends WindowPacket {
	private int x;
	private int y;
	private int width;
	private int height;

	public MapWindowPacket(int windowId, int x, int y, int width, int height) {
		super(windowId, PacketType.MAP_WINDOW);
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	public MapWindowPacket(NewWindowPacket packet) {
		this(packet.getWindowId(), packet.getX(), packet.getY(), packet.getWidth(), packet.getHeight());
	}

	public MapWindowPacket(List<Object> list) {
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

	@Override
	public String toString() {
		return "MapWindowPacket [x=" + x + ", y=" + y + ", width=" + width + ", height=" + height + ", windowId="
				+ windowId + ", type=" + type + "]";
	}
}
