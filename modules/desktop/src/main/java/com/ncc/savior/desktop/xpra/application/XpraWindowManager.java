package com.ncc.savior.desktop.xpra.application;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.xpra.XpraClient;
import com.ncc.savior.desktop.xpra.protocol.IPacketHandler;
import com.ncc.savior.desktop.xpra.protocol.IPacketSender;
import com.ncc.savior.desktop.xpra.protocol.packet.PacketType;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.DrawPacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.LostWindowPacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.NewWindowPacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.Packet;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.WindowMoveResizePacket;

public abstract class XpraWindowManager implements IPacketHandler {
	private static final Logger logger = LoggerFactory.getLogger(XpraWindowManager.class);

	private XpraClient client;
	private HashSet<PacketType> packetTypes;
	private HashMap<Integer, IXpraWindow> windows;

	protected boolean debugOutput;

	public XpraWindowManager(XpraClient client) {
		this.client = client;
		this.packetTypes = new HashSet<PacketType>();
		this.packetTypes.add(PacketType.NEW_WINDOW);
		this.packetTypes.add(PacketType.NEW_WINDOW_OVERRIDE_REDIRECT);
		this.packetTypes.add(PacketType.LOST_WINDOW);
		this.packetTypes.add(PacketType.WINDOW_MOVE_RESIZE);
		this.packetTypes.add(PacketType.DRAW);
		this.client = client;
		this.client.addPacketListener(this);
		this.windows = new HashMap<Integer, IXpraWindow>();
		this.debugOutput = false;
	}

	@Override
	public void handlePacket(Packet packet) {
		switch(packet.getType()) {
		case NEW_WINDOW_OVERRIDE_REDIRECT:
		case NEW_WINDOW:
			onNewWindow((NewWindowPacket) (packet));
			break;
		case LOST_WINDOW:
			onLostWindow((LostWindowPacket) (packet));
			break;
		case WINDOW_MOVE_RESIZE:
			onWindowMoveResize((WindowMoveResizePacket) packet);
			break;
		case DRAW:
			onDraw((DrawPacket) packet);
			break;
		default:
			break;
		}
	}

	private void onDraw(DrawPacket packet) {
		IXpraWindow window = windows.get(packet.getWindowId());
		if (window != null) {
			window.draw(packet);
		} else {
			logger.error("Unable to find window to be drawn to.  ID=" + packet.getWindowId() + " Packet=" + packet);
		}
	}

	private void onWindowMoveResize(WindowMoveResizePacket packet) {
		IXpraWindow window = windows.get(packet.getWindowId());
		if (window != null) {
			doWindowMoveResize(packet);
			window.onWindowMoveResize(packet);
		} else {
			logger.error("Unable to find window to be drawn to.  ID=" + packet.getWindowId() + " Packet=" + packet);
		}

	}

	private void onLostWindow(LostWindowPacket lostWindowPacket) {
		IXpraWindow window = windows.get(lostWindowPacket.getWindowId());
		if (window != null) {
			window.close();
			doRemoveWindow(lostWindowPacket, window);
			windows.remove(lostWindowPacket.getWindowId());
		} else {
			logger.error("Unable to find window to be closed.  ID=" + lostWindowPacket.getWindowId() + " Packet="
					+ lostWindowPacket);
		}

	}

	protected abstract void doRemoveWindow(LostWindowPacket lostWindowPacket, IXpraWindow window);

	public void setDebugOutput(boolean debugOutput) {
		this.debugOutput = debugOutput;
		for (Entry<Integer, IXpraWindow> entry : windows.entrySet()) {
			IXpraWindow window = entry.getValue();
			window.setDebugOutput(debugOutput);
		}
	}

	private void onNewWindow(NewWindowPacket packet) {
		int id = packet.getWindowId();
		IXpraWindow window = createNewWindow(packet, client.getPacketSender());
		window.setDebugOutput(debugOutput);
		windows.put(id, window);
	}

	protected abstract void doWindowMoveResize(WindowMoveResizePacket packet);

	protected abstract IXpraWindow createNewWindow(NewWindowPacket packet, IPacketSender iPacketSender);

	@Override
	public Set<PacketType> getValidPacketTypes() {
		return packetTypes;
	}
}
