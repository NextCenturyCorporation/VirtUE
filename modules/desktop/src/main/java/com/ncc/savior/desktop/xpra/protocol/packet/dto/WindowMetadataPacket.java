package com.ncc.savior.desktop.xpra.protocol.packet.dto;

import java.util.List;
import java.util.Map;

import com.ncc.savior.desktop.xpra.protocol.packet.PacketType;
import com.ncc.savior.desktop.xpra.protocol.packet.PacketUtils;

/**
 * Packet representing an update to the metadata for a window. See
 * {@link WindowMetadata} for details on what can be included.
 *
 *
 */
public class WindowMetadataPacket extends WindowPacket {

	private Map<String, Object> metadataRaw;
	private WindowMetadata metadata;

	public WindowMetadataPacket(int windowId, Map<String, Object> metadata) {
		super(windowId, PacketType.WINDOW_METADATA);
		this.metadataRaw = metadata;
		this.metadata = new WindowMetadata(metadataRaw);
	}

	public WindowMetadataPacket(List<Object> list) {
		this(PacketUtils.asInt(list.get(1)), PacketUtils.asStringObjectMap(list, 2));
	}

	@Override
	protected void doAddToList(List<Object> list) {
		list.add(windowId);
		list.add(metadataRaw);
	}

	public Map<String, Object> getMetadataRaw() {
		return metadataRaw;
	}

	public WindowMetadata getMetadata() {
		return metadata;
	}

	@Override
	public String toString() {
		return "WindowMetadataPacket [metadataRaw=" + metadataRaw + ", metadata=" + metadata + ", windowId=" + windowId
				+ ", type=" + type + "]";
	}
}
