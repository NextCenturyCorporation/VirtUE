package com.ncc.savior.desktop.xpra.protocol.packet.dto;

import java.util.ArrayList;

import com.ncc.savior.desktop.xpra.protocol.packet.PacketType;

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

	@Override
	protected void doAddToList(ArrayList<Object> list) {
		list.add(windowId);
		list.add(x);
		list.add(y);
		list.add(width);
		list.add(height);
	}
}
