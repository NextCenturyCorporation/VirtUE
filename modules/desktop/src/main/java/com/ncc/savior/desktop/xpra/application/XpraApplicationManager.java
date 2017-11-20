package com.ncc.savior.desktop.xpra.application;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.xpra.XpraClient;
import com.ncc.savior.desktop.xpra.protocol.IPacketHandler;
import com.ncc.savior.desktop.xpra.protocol.packet.DisconnectPacket;
import com.ncc.savior.desktop.xpra.protocol.packet.PacketType;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.ConfigureOverrideRedirectPacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.InitiateMoveResizePacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.LostWindowPacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.NewWindowOverrideRedirectPacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.NewWindowPacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.Packet;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.RaiseWindowPacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.StartupCompletePacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.UnMapWindowPacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.WindowMetadata;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.WindowMetadataPacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.WindowMoveResizePacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.WindowPacket;

/**
 * This is the abstract base class that manages all the {@link XpraApplication}s
 * created by the connection. Most connections will have a single application,
 * but some applications (browsers for example), can open many OS windows.
 *
 */
public abstract class XpraApplicationManager {
	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(XpraApplicationManager.class);
	protected XpraClient client;
	protected Map<Integer, XpraApplication> applications;
	protected Map<Integer, XpraApplication> windowIdsToApplications;
	private boolean show = false;
	private boolean setDebugOutput;

	public XpraApplicationManager(XpraClient client) {
		this.client = client;
		this.applications = new HashMap<Integer, XpraApplication>();
		this.windowIdsToApplications = new HashMap<Integer, XpraApplication>();
		initPacketHandling();
	}

	public void setDebugOutput(boolean b) {
		this.setDebugOutput = b;
		for (XpraApplication app : applications.values()) {
			app.setDebugOutput(true);
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
				// These are actions that need to be handled by the application, not the window.
				switch (packet.getType()) {
				case NEW_WINDOW:
					NewWindowPacket p = (NewWindowPacket) packet;
					int parentId = p.getMetadata().getParentId();
					boolean isModal = p.getMetadata().getModal();
					if (parentId > 0 && isModal) {
						XpraApplication parent = applications.get(parentId);
						onModal(p, parent);
					} else {
						onNewWindow(p);
					}
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
				case RAISE_WINDOW:
					onRaiseWindow((RaiseWindowPacket) packet);
					break;
				case WINDOW_METADATA:
					onMetadatPacket((WindowMetadataPacket) packet);
					break;
				case INITIATE_MOVERESIZE:
					onInitMoveResize((InitiateMoveResizePacket) packet);
					break;
				case CONFIGURE_OVERRIDE_REDIRECT:
					onConfigureRedirectOverride((ConfigureOverrideRedirectPacket) packet);
					break;
				case WINDOW_MOVE_RESIZE:
					onWindowMoveResize((WindowMoveResizePacket) packet);
					break;
				case UNMAP_WINDOW:
					onUnMapWindow((UnMapWindowPacket) packet);
				case DISCONNECT:
					onDisconnect((DisconnectPacket) packet);
					break;
				default:

				}
				// these will be passed to their implementation of XpraWindow
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

	protected void onUnMapWindow(UnMapWindowPacket packet) {
		XpraApplication app = applications.get(packet.getWindowId());
		app.minimize();
	}

	protected void onInitMoveResize(InitiateMoveResizePacket packet) {
		XpraApplication app = applications.get(packet.getWindowId());
		app.initiateMoveResize(packet);
	}

	protected void onMetadatPacket(WindowMetadataPacket packet) {
		WindowMetadata meta = packet.getMetadata();
		XpraApplication app = applications.get(packet.getWindowId());
		Boolean minimized = meta.getIconicOrNull();
		if (minimized != null) {
			if (minimized) {
				app.minimize();
			} else {
				app.restore();
			}
		}
		Boolean maximized = meta.getMaximizedOrNull();
		if (maximized != null) {
			if (maximized) {
				app.maximize();
			} else {
				app.unMaximize();
			}
		}
	}

	protected void onDisconnect(DisconnectPacket packet) {
		client.close();
		for (XpraApplication app : applications.values()) {
			try {
				app.close();
			} catch (IOException e) {
				logger.error("Error closing app=" + app);
			}
		}
	}

	protected void onRaiseWindow(RaiseWindowPacket packet) {
		// do nothing here, window will handle it.
	}

	protected void onStartupComplete(StartupCompletePacket packet) {
		show = true;
		for (XpraApplication app : applications.values()) {
			app.Show();
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

	protected void onModal(NewWindowPacket packet, XpraApplication parent) {
		// int parentId = packet.getMetadata().getParentId();
		// int id = packet.getWindowId();
		// windowIdsToApplications.put(id, windowIdsToApplications.get(parentId));

		XpraApplication app = createXpraApplication(packet, parent);
		app.setDebugOutput(this.setDebugOutput);
		if (show) {
			app.Show();
		}
		int baseWindowId = packet.getWindowId();
		applications.put(baseWindowId, app);
		windowIdsToApplications.put(baseWindowId, app);
	}

	protected void onNewWindowOverride(NewWindowOverrideRedirectPacket packet) {
		int id = packet.getWindowId();
		XpraApplication parent = getParentApplication(packet.getWindowId(), packet.getMetadata());
		if (parent != null) {
			windowIdsToApplications.put(id, parent);
		} else {
			onNewWindow(packet);
		}
	}

	protected void onConfigureRedirectOverride(ConfigureOverrideRedirectPacket packet) {
		int wid = packet.getWindowId();
		XpraApplication app = applications.get(wid);
		if (app != null) {
			app.setLocationSize(packet.getX(), packet.getY(), packet.getWidth(), packet.getHeight());
		} else {
			logger.warn("Recieved packet with valid application. packet=" + packet);
		}
	}

	protected void onWindowMoveResize(WindowMoveResizePacket packet) {
		int wid = packet.getWindowId();
		XpraApplication app = applications.get(wid);
		if (app != null) {
			app.setLocationSize(packet.getX(), packet.getY(), packet.getWidth(), packet.getHeight());
		} else {
			logger.warn("Recieved packet with valid application. packet=" + packet);
		}
	}

	private XpraApplication getParentApplication(int windowId, WindowMetadata windowMetadata) {
		XpraApplication parent = windowIdsToApplications.get(windowMetadata.getParentId());
		return parent;
	}

	protected void onLostWindow(LostWindowPacket packet) {
		int id = packet.getWindowId();
		if (applications.containsKey(id)) {
			applications.remove(id);
		}
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

	protected abstract XpraApplication createXpraApplication(NewWindowPacket packet);

	protected abstract XpraApplication createXpraApplication(NewWindowPacket packet, XpraApplication parent);
}
