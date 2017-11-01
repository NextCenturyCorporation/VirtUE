package com.ncc.savior.desktop.xpra.protocol.packet.dto;

import java.util.List;

import com.ncc.savior.desktop.xpra.protocol.packet.PacketType;

/**
 * New window override redirect packets indicate there is a new window that is
 * not enclosed in a previous window.
 *
 *
 */
public class NewWindowOverrideRedirectPacket extends NewWindowPacket {

	public NewWindowOverrideRedirectPacket(List<Object> list) {
		super(list);
		this.type = PacketType.NEW_WINDOW_OVERRIDE_REDIRECT;
	}
}
