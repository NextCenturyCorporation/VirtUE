package com.ncc.savior.desktop.xpra.protocol.packet.dto;

import java.util.ArrayList;
import java.util.List;

import com.ncc.savior.desktop.xpra.protocol.packet.PacketType;
import com.ncc.savior.desktop.xpra.protocol.packet.PacketUtils;

/**
 * Packet sent by Client when it has modified a window in some way.
 *
 *
 */
public class ConfigureWindowPacket extends Packet {
	private int windowId;
	private int x;
	private int y;
	private int width;
	private int height;

	public ConfigureWindowPacket(int windowId, int x, int y, int width, int height) {
		super(PacketType.CONFIGURE_WINDOW);
		this.windowId = windowId;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
    }

	public ConfigureWindowPacket(List<Object> list) {
    	super(PacketType.CONFIGURE_WINDOW);
		this.windowId = PacketUtils.asInt(list.get(1));
		this.x = PacketUtils.asInt(list.get(2));
		this.y = PacketUtils.asInt(list.get(3));
		this.width = PacketUtils.asInt(list.get(4));
		this.height = PacketUtils.asInt(list.get(5));
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
		return "ConfigureWindowPacket [windowId=" + windowId + ", x=" + x + ", y=" + y + ", width=" + width
				+ ", height=" + height + ", type=" + type + "]";
	}

}
