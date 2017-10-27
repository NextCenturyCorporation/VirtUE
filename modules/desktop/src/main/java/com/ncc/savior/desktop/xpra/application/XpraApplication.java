package com.ncc.savior.desktop.xpra.application;

import com.ncc.savior.desktop.xpra.XpraClient;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.WindowPacket;

/**
 * DESCRIPTION TODO
 *
 * Implementations need to set windowManager in their constructor!
 *
 *
 */
public abstract class XpraApplication {
	protected XpraClient client;
	protected XpraWindowManager windowManager;
	protected int baseWindowId;

	public XpraApplication(XpraClient client, int baseWindowId) {
		this.client = client;
		this.baseWindowId = baseWindowId;
	}

	public int getBaseWindowId() {
		return baseWindowId;
	}

	public void setDebugOutput(boolean debugOutput) {
		windowManager.setDebugOutput(debugOutput);
	}

	public void handleWindowPacket(WindowPacket p) {
		if (windowManager.getValidPacketTypes().contains(p.getType())) {
			windowManager.handlePacket(p);
		}
	}

	public abstract void Show();
}
