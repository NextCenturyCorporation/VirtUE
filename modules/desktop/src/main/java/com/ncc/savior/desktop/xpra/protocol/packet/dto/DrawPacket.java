package com.ncc.savior.desktop.xpra.protocol.packet.dto;

import java.util.List;

import com.ncc.savior.desktop.xpra.protocol.ImageEncoding;
import com.ncc.savior.desktop.xpra.protocol.packet.PacketType;
import com.ncc.savior.desktop.xpra.protocol.packet.PacketUtils;

/**
 * Packet sent by server to client containing updated pixel data for the client
 * to draw. The server often does not send the entire window unless the entire
 * window has changed.
 *
 *
 */
public class DrawPacket extends WindowPacket implements IImagePacket {
	private int x;
	private int y;
	private int width;
	private int height;
	private ImageEncoding encoding;
	private byte[] data;
	private int sequence;
	private int rowstride;

	protected DrawPacket(int windowId, int x, int y, int width, int height, ImageEncoding encoding, byte[] data,
			int sequence, int rowstride) {
		super(windowId, PacketType.DRAW);
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.encoding = encoding;
		this.data = data;
		this.sequence = sequence;
		this.rowstride = rowstride;
	}

	public DrawPacket(List<Object> list) {
		super(PacketUtils.asInt(list.get(1)), PacketType.DRAW);
		this.x = PacketUtils.asInt(list.get(2));
		this.y = PacketUtils.asInt(list.get(3));
		this.width = PacketUtils.asInt(list.get(4));
		this.height = PacketUtils.asInt(list.get(5));
		this.encoding = ImageEncoding.parse(PacketUtils.asString(list.get(6)));
		this.data = (byte[]) list.get(7);
		this.sequence = PacketUtils.asInt(list.get(8));
		this.rowstride = PacketUtils.asInt(list.get(9));
	}

	@Override
	protected void doAddToList(List<Object> list) {
		list.add(windowId);
		list.add(x);
		list.add(y);
		list.add(width);
		list.add(height);
		list.add(encoding.toString());
		list.add(data);
		list.add(sequence);
		list.add(rowstride);
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
	public ImageEncoding getEncoding() {
		return encoding;
	}

	@Override
	public byte[] getData() {
		return data;
	}

	public int getSequence() {
		return sequence;
	}

	public int getRowstride() {
		return rowstride;
	}

	@Override
	public String toString() {
		return "DrawPacket [windowId=" + windowId + ", x=" + x + ", y=" + y + ", width=" + width + ", height=" + height
				+ ", encoding=" + encoding + ", dataSize=" + data.length + ", sequence=" + sequence + ", rowstride="
				+ rowstride + ", type=" + type + "]";
	}

}
