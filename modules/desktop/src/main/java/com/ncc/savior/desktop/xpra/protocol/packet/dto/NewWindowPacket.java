package com.ncc.savior.desktop.xpra.protocol.packet.dto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ncc.savior.desktop.xpra.protocol.packet.PacketType;
import com.ncc.savior.desktop.xpra.protocol.packet.PacketUtils;

/**
 * Packet sent by server to inform the client that there is a new window to
 * display. The client should send a 'map-window' {@link Packet} back to tell
 * the server where the window is. Draw information will be sent in a future
 * {@link DrawPacket} and is not contained in this packet.
 *
 * Much of the metadata and 'other' parameter still needs to be investigated.
 *
 *
 */
public class NewWindowPacket extends WindowPacket {

	private int x;
	private int y;
	private int width;
	private int height;
	private Map<String, Object> metadataRaw;
	private WindowMetadata metadata;
	private Map<String, Object> otherData;

	protected NewWindowPacket(int windowId, int x, int y, int w, int h, Map<String, Object> metadata,
			Map<String, Object> other) {
		super(windowId, PacketType.NEW_WINDOW);
		this.x = x;
		this.y = y;
		this.width = w;
		this.height = h;
		this.metadataRaw = metadata;
		this.metadata = new WindowMetadata(metadataRaw);
		// Empty from what I've seen...
		this.otherData = new HashMap<String, Object>();
	}

	public NewWindowPacket(List<Object> list) {
		this(PacketUtils.asInt(list.get(1)), PacketUtils.asInt(list.get(2)), PacketUtils.asInt(list.get(3)),
				PacketUtils.asInt(list.get(4)), PacketUtils.asInt(list.get(5)), PacketUtils.asStringObjectMap(list, 6),
				PacketUtils.asStringObjectMap(list, 7));

	}

	@Override
	protected void doAddToList(List<Object> list) {
		list.add(windowId);
		list.add(x);
		list.add(y);
		list.add(width);
		list.add(height);
		list.add(metadataRaw);
		list.add(otherData);
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

	public Map<String, Object> getMetadataRaw() {
		return metadataRaw;
	}

	public WindowMetadata getMetadata() {
		return metadata;
	}

	public Map<String, Object> getOtherData() {
		return otherData;
	}

	@Override
	public String toString() {
		return "NewWindowPacket [windowId=" + windowId + ", x=" + x + ", y=" + y + ", width=" + width + ", height="
				+ height + ", metadata=" + metadata + ", otherData=" + otherData + ", type=" + type + "]";
	}

	public void overrideXy(int x, int y) {
		this.x = x;
		this.y = y;

	}

}
