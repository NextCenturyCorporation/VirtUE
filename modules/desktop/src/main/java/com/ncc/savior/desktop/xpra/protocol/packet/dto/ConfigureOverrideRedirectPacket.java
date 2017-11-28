package com.ncc.savior.desktop.xpra.protocol.packet.dto;

import java.util.List;

import com.ncc.savior.desktop.xpra.protocol.packet.PacketType;

public class ConfigureOverrideRedirectPacket extends ConfigureWindowPacket {

	public ConfigureOverrideRedirectPacket(int windowId, int x, int y, int width, int height) {
		super(windowId, x, y, width, height);
		this.type = PacketType.CONFIGURE_OVERRIDE_REDIRECT;
	}

	public ConfigureOverrideRedirectPacket(List<Object> list) {
		super(list);
		this.type = PacketType.CONFIGURE_OVERRIDE_REDIRECT;
	}
}
