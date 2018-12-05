package com.ncc.savior.desktop.xpra.protocol.packet.dto;

import java.util.HashMap;
import java.util.List;

import com.ncc.savior.desktop.xpra.protocol.packet.PacketType;
import com.ncc.savior.desktop.xpra.protocol.packet.PacketUtils;

public class BufferRefreshPacket extends WindowPacket {

	private int unused;
	private int quality;
	private HashMap<String, String> refreshOptions;
	private HashMap<String, String> clientProperties;

	public BufferRefreshPacket(int windowId) {
		super(windowId, PacketType.BUFFER_REFRESH);
		this.unused = 0;
		this.quality = 100;
		this.refreshOptions = new HashMap<String, String>();
		this.clientProperties = new HashMap<String, String>();
	}

	public BufferRefreshPacket(List<Object> list) {
		this(PacketUtils.asInt(list.get(1)));

	}

	@Override
	protected void doAddToList(List<Object> list) {
		list.add(windowId);
		list.add(unused);
		list.add(quality);
		list.add(refreshOptions);
		list.add(clientProperties);
	}

	public int getUnused() {
		return unused;
	}

	public int getQuality() {
		return quality;
	}

	public HashMap<String, String> getRefreshOptions() {
		return refreshOptions;
	}

	public HashMap<String, String> getClientProperties() {
		return clientProperties;
	}
}
