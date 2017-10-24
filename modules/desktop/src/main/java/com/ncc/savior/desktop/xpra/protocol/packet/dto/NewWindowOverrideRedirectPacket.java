package com.ncc.savior.desktop.xpra.protocol.packet.dto;

import java.util.List;

import com.ncc.savior.desktop.xpra.protocol.packet.PacketType;

public class NewWindowOverrideRedirectPacket extends NewWindowPacket {

	public NewWindowOverrideRedirectPacket(List<Object> list) {
		super(list);
		this.type = PacketType.NEW_WINDOW_OVERRIDE_REDIRECT;
	}
}
