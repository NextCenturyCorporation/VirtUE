package com.ncc.savior.desktop.xpra.application;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.xpra.XpraClient;
import com.ncc.savior.desktop.xpra.protocol.IPacketHandler;
import com.ncc.savior.desktop.xpra.protocol.keyboard.BruteForceKeyMap;
import com.ncc.savior.desktop.xpra.protocol.packet.PacketType;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.LostWindowPacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.NewWindowOverrideRedirectPacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.NewWindowPacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.Packet;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.StartupCompletePacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.WindowPacket;

public abstract class XpraApplicationManager {
	private static final Logger logger = LoggerFactory.getLogger(XpraApplicationManager.class);
	protected XpraClient client;
	protected Map<Integer, XpraApplication> applications;
	protected Map<Integer, XpraApplication> windowIdsToApplications;
	protected BruteForceKeyMap keyMap;
	private boolean show = false;

	public XpraApplicationManager(XpraClient client) {
		this.client = client;
		this.keyMap = new BruteForceKeyMap();
		this.applications = new HashMap<Integer, XpraApplication>();
		this.windowIdsToApplications = new HashMap<Integer, XpraApplication>();
		initPacketHandling();
	}

	public void setDebugOutput(boolean b) {
		for (XpraApplication app : applications.values()) {
			app.setDebugOutput(true);
		}
	}

	private void onNewWindow(NewWindowPacket packet) {
		XpraApplication app = createXpraApplication(packet);
		if (show) {
			app.Show();
		}
		int baseWindowId = packet.getWindowId();
		applications.put(baseWindowId, app);
		windowIdsToApplications.put(baseWindowId, app);
	}

	protected abstract XpraApplication createXpraApplication(NewWindowPacket packet);

	protected void onLostWindow(LostWindowPacket packet) {
		int id = packet.getWindowId();
		if (applications.containsKey(id)) {
			applications.remove(id);
		}
	}

	private void initPacketHandling() {
		IPacketHandler handler = new IPacketHandler() {
			{
				initPacketTypes();
			}

			private Set<PacketType> packetTypes;

			@Override
			public void handlePacket(Packet packet) {
				switch (packet.getType()) {
				case NEW_WINDOW:
					onNewWindow((NewWindowPacket) packet);
					break;
				case LOST_WINDOW:
					onLostWindow((LostWindowPacket) packet);
					break;
				case NEW_WINDOW_OVERRIDE_REDIRECT:
					onNewWindowOverride((NewWindowOverrideRedirectPacket) packet);
					break;
				case STARTUP_COMPLETE:
					onStartupComplete((StartupCompletePacket) packet);
					break;
				default:


				}
				if (packet instanceof WindowPacket) {
					WindowPacket p = (WindowPacket) packet;
					handleWindowPacket(p);
				} else {
					handleNonWindowPacket(packet);
				}
			}

			private void initPacketTypes() {
				packetTypes = new HashSet<PacketType>();
				// packetTypes.add(PacketType.NEW_WINDOW);
				// packetTypes.add(PacketType.LOST_WINDOW);
			}

			@Override
			public Set<PacketType> getValidPacketTypes() {
				return packetTypes;
			}
		};
		client.addPacketListener(handler);
	}

	protected void onStartupComplete(StartupCompletePacket packet) {
		show = true;
		for (XpraApplication app : applications.values()) {
			app.Show();
		}
	}

	protected void onNewWindowOverride(NewWindowOverrideRedirectPacket packet) {
		int parentId = packet.getMetadata().getParentId();
		int id = packet.getWindowId();
		windowIdsToApplications.put(id, windowIdsToApplications.get(parentId));
	}

	protected void handleNonWindowPacket(Packet packet) {
		// logger.debug("NonWindowPacket: Packet=" + packet);
	}

	protected void handleWindowPacket(WindowPacket p) {
		int id = p.getWindowId();
		XpraApplication app = windowIdsToApplications.get(id);
		if (app != null) {
			app.handleWindowPacket(p);
		}
	}
}
