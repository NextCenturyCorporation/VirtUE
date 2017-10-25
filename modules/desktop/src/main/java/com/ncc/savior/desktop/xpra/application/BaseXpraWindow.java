package com.ncc.savior.desktop.xpra.application;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.xpra.protocol.IPacketSender;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.DamageSequencePacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.DrawPacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.MouseButtonActionPacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.MousePointerPositionPacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.NewWindowPacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.Packet;

public abstract class BaseXpraWindow implements IXpraWindow {
	private static final Logger logger = LoggerFactory.getLogger(BaseXpraWindow.class);

	protected int id;
	protected boolean debugOutput;
	protected IPacketSender packetSender;

	public BaseXpraWindow(NewWindowPacket packet, IPacketSender packetSender) {
		this.id = packet.getWindowId();
		this.packetSender = packetSender;
	}

	@Override
	public void setDebugOutput(boolean debugOn) {
		this.debugOutput = debugOn;
	}

	protected void sendDamageSequence(DrawPacket packet) {
		DamageSequencePacket sendPacket = new DamageSequencePacket(packet);
		sendPacket(sendPacket, "damage packet");
	}

	protected void onMouseMove(int x, int y, List<String> modifiers) {
		MousePointerPositionPacket sendPacket = new MousePointerPositionPacket(id, x, y, modifiers);
		sendPacket(sendPacket, "pointer position packet");
	}

	protected void onMousePress(int button, int x, int y, List<String> modifiers) {
		MouseButtonActionPacket sendPacket = new MouseButtonActionPacket(id, button, true, x, y, modifiers);
		sendPacket(sendPacket, "mouse button action packet (pressed)");
	}

	protected void onMouseRelease(int button, int x, int y, List<String> modifiers) {
		MouseButtonActionPacket sendPacket = new MouseButtonActionPacket(id, button, false, x, y, modifiers);
		sendPacket(sendPacket, "mouse button action packet (released)");
	}

	private void sendPacket(Packet sendPacket, String packetDescription) {
		try {
			packetSender.sendPacket(sendPacket);
			if (logger.isDebugEnabled()) {
				// logger.debug("Sending Packet=" + sendPacket.toString());
			}
		} catch (IOException e) {
			logger.error("Error attempting to send damage packet=" + sendPacket, e);
		}
	}

}
