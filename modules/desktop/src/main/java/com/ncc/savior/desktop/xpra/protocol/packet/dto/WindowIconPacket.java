package com.ncc.savior.desktop.xpra.protocol.packet.dto;

import java.util.ArrayList;
import java.util.List;

import com.ncc.savior.desktop.xpra.protocol.ImageEncoding;
import com.ncc.savior.desktop.xpra.protocol.packet.PacketType;
import com.ncc.savior.desktop.xpra.protocol.packet.PacketUtils;

/**
 * Packet that indicate the icon for the window has changed. This icon usually
 * appears in the top left of the window as well as any taskbar from the OS.
 *
 *
 */
public class WindowIconPacket extends WindowPacket implements IImagePacket {

	private int width;
	private int height;
	private ImageEncoding encoding;
	private byte[] data;

	protected WindowIconPacket(int windowId, int width, int height, ImageEncoding encoding, byte[] data) {
		super(windowId, PacketType.WINDOW_ICON);
		this.width = width;
		this.height = height;
		this.encoding = encoding;
		this.data = data;
	}

	public WindowIconPacket(List<Object> list) {
		super(PacketUtils.asInt(list.get(1)), PacketType.WINDOW_ICON);
		this.width = PacketUtils.asInt(list.get(2));
		this.height = PacketUtils.asInt(list.get(3));
		this.encoding = ImageEncoding.parse(PacketUtils.asString(list.get(4)));
		this.data = (byte[]) list.get(5);
	}

	@Override
	protected void doAddToList(ArrayList<Object> list) {
		list.add(windowId);
		list.add(width);
		list.add(height);
		list.add(encoding.getCode());
		list.add(data);
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	@Override
	public ImageEncoding getEncoding() {
		return encoding;
	}

	@Override
	public byte[] getData() {
		return data;
	}
}
