package com.ncc.savior.desktop.xpra.application;

import java.io.Closeable;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.xpra.XpraClient;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.ConfigureWindowPacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.MapWindowPacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.Packet;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.UnMapWindowPacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.WindowPacket;

/**
 * Abstract base class for an Application. An application is a collection of
 * {@link XpraWindow}s controled by an {@link XpraWindowManager} that has its
 * own entry in the OS taskbar.
 *
 * Implementations need to set windowManager in their constructor!
 *
 *
 */
public abstract class XpraApplication implements Closeable {
	private static final Logger logger = LoggerFactory.getLogger(XpraApplication.class);
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

	protected void onRestored(int x, int y, int width, int height) {
		MapWindowPacket sendPacket = new MapWindowPacket(baseWindowId, x, y, width, height);
		sendPacket(sendPacket, "Window restored");
	}

	protected void onMinimized() {
		UnMapWindowPacket sendPacket = new UnMapWindowPacket(baseWindowId);
		sendPacket(sendPacket, "window minimized");
	}

	protected void onSizeChange(int width, int height) {
		ConfigureWindowPacket sendPacket = new ConfigureWindowPacket(baseWindowId, 0, 0, width, height);
		sendPacket(sendPacket, "Configure Window");
	}

	public abstract void Show();

	public abstract void doClose() throws IOException;

	@Override
	public void close() throws IOException {
		windowManager.close();
		doClose();
	}

	protected void sendPacket(Packet sendPacket, String packetDescription) {
		try {
			client.getPacketSender().sendPacket(sendPacket);
			if (logger.isDebugEnabled()) {
				// logger.debug("Sending Packet=" + sendPacket.toString());
			}
		} catch (IOException e) {
			logger.error("Error attempting to send packet=" + sendPacket, e);
		}
	}
}
