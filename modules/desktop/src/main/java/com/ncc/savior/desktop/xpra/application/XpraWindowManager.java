package com.ncc.savior.desktop.xpra.application;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.xpra.XpraClient;
import com.ncc.savior.desktop.xpra.protocol.IPacketHandler;
import com.ncc.savior.desktop.xpra.protocol.IPacketSender;
import com.ncc.savior.desktop.xpra.protocol.keyboard.KeyCodeDto;
import com.ncc.savior.desktop.xpra.protocol.packet.PacketType;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.CloseWindowPacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.DrawPacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.FocusPacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.KeyActionPacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.LostWindowPacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.NewWindowOverrideRedirectPacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.NewWindowPacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.Packet;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.WindowIconPacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.WindowMetadataPacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.WindowMoveResizePacket;

/**
 * Manages a bunch of {@link XpraWindow}s for an {@link XpraApplication}. A
 * window is defined by the Xpra protocol and is considered any panel or screen
 * that appears overtop another view. For example, tooltips, history, or
 * settings panels are often their own window inside a single application.
 *
 *
 */
public abstract class XpraWindowManager implements IPacketHandler, IFocusNotifier, Closeable {
	private static final Logger logger = LoggerFactory.getLogger(XpraWindowManager.class);

	protected XpraClient client;
	private HashSet<PacketType> packetTypes;
	private HashMap<Integer, IXpraWindow> windows;

	protected boolean debugOutput;
	protected int focusedWindowId;

	private Queue<Packet> packetQueue;

	private boolean graphicsInit = false;

	protected int baseWindowId;;

	public XpraWindowManager(XpraClient client, int baseWindowId) {
		this.client = client;
		this.packetTypes = new HashSet<PacketType>();
		this.packetTypes.add(PacketType.NEW_WINDOW);
		this.packetTypes.add(PacketType.NEW_WINDOW_OVERRIDE_REDIRECT);
		this.packetTypes.add(PacketType.LOST_WINDOW);
		this.packetTypes.add(PacketType.WINDOW_MOVE_RESIZE);
		this.packetTypes.add(PacketType.DRAW);
		this.packetTypes.add(PacketType.WINDOW_ICON);
		this.packetTypes.add(PacketType.WINDOW_METADATA);
		this.client = client;
		this.windows = new HashMap<Integer, IXpraWindow>();
		this.debugOutput = false;
		this.graphicsInit = false;
		this.packetQueue = new LinkedBlockingQueue<Packet>();
		this.baseWindowId = baseWindowId;
	}

	protected synchronized void setGraphicsInit() {
		Packet packet = null;
		while (null != (packet = packetQueue.poll())) {
			internalHandlePacket(packet);
		}
		graphicsInit = true;
		packetQueue = null;
	}

	@Override
	public synchronized void handlePacket(Packet packet) {
		if (!graphicsInit) {
			packetQueue.add(packet);
		} else {
			internalHandlePacket(packet);
		}
	}

	private void internalHandlePacket(Packet packet) {
		switch (packet.getType()) {
		case NEW_WINDOW_OVERRIDE_REDIRECT:
			onNewWindowOverride((NewWindowOverrideRedirectPacket) packet);
			break;
		case NEW_WINDOW:
			onNewWindow((NewWindowPacket) (packet));
			break;
		case WINDOW_METADATA:
			onMetadataUpdate((WindowMetadataPacket) packet);
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
		case WINDOW_ICON:
			onWindowIcon((WindowIconPacket) packet);
			break;
		default:

			break;
		}
	}

	public void onWindowLostFocus() {
		FocusPacket sendPacket = new FocusPacket(0);
		this.notifyFocusedWindow(0);
		sendPacket(sendPacket, "focus packet");
	}

	@Override
	public Set<PacketType> getValidPacketTypes() {
		return packetTypes;
	}

	public void setDebugOutput(boolean debugOutput) {
		this.debugOutput = debugOutput;
		for (Entry<Integer, IXpraWindow> entry : windows.entrySet()) {
			IXpraWindow window = entry.getValue();
			window.setDebugOutput(debugOutput);
		}
	}

	@Override
	public void notifyFocusedWindow(int windowId) {
		this.focusedWindowId = windowId;
	}

	@Override
	public void close() throws IOException {
		for (IXpraWindow win : windows.values()) {
			win.close();
		}
		doClose();
	}

	private void onMetadataUpdate(WindowMetadataPacket packet) {
		IXpraWindow window = windows.get(packet.getWindowId());
		if (window != null) {
			window.updateWindowMetadata(packet);
		} else {
			logger.error("Unable to find window to set icon on.  ID=" + packet.getWindowId() + " Packet=" + packet);
		}
	}

	private void onWindowIcon(WindowIconPacket packet) {
		IXpraWindow window = windows.get(packet.getWindowId());
		if (window != null) {
			window.setWindowIcon(packet);
		} else {
			logger.error("Unable to find window to set icon on.  ID=" + packet.getWindowId() + " Packet=" + packet);
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
			try {
				window.close();
			} catch (IOException e) {
				logger.error("Error attempting to close window.  Window=" + window);
			}
			doRemoveWindow(lostWindowPacket, window);
			windows.remove(lostWindowPacket.getWindowId());
			if (windows.isEmpty()) {
				try {
					this.close();
				} catch (IOException e) {
					logger.error("Error attempting to close WindowManager=" + this);
				}
			}
		} else {
			logger.error("Unable to find window to be closed.  ID=" + lostWindowPacket.getWindowId() + " Packet="
					+ lostWindowPacket);
		}
	}

	protected void onKeyDown(KeyCodeDto key, List<String> mods) {
		int keyval = key.getKeyVal();
		int keycode = key.getKeyCode();
		String keyname = key.getKeyName();
		int group = key.getGroup();
		int id = focusedWindowId;
		KeyActionPacket sendPacket = new KeyActionPacket(id, keyval, keycode, keyname, true, group, mods);
		// logger.debug(sendPacket.toString());
		sendPacket(sendPacket, "key action packet (pressed)");
	}

	protected void onKeyUp(KeyCodeDto key, List<String> mods) {
		int keyval = key.getKeyVal();
		int keycode = key.getKeyCode();
		String keyname = key.getKeyName();
		int group = key.getGroup();
		int id = focusedWindowId;
		KeyActionPacket sendPacket = new KeyActionPacket(id, keyval, keycode, keyname, false, group, mods);
		sendPacket(sendPacket, "key action packet (released)");
	}

	private void sendPacket(Packet sendPacket, String packetDescription) {
		try {
			client.getPacketSender().sendPacket(sendPacket);
			if (logger.isDebugEnabled()) {
				// logger.debug("Sending Packet=" + sendPacket.toString());
			}
		} catch (IOException e) {
			logger.error("Error attempting to send packet=" + sendPacket, e);
		}
	}

	private void onNewWindow(NewWindowPacket packet) {
		int id = packet.getWindowId();
		IXpraWindow window = createNewWindow(packet, client.getPacketSender());
		window.setDebugOutput(debugOutput);
		windows.put(id, window);
	}

	private void onNewWindowOverride(NewWindowOverrideRedirectPacket packet) {
		int id = packet.getWindowId();
		IXpraWindow window = createNewWindow(packet, client.getPacketSender());
		window.setDebugOutput(debugOutput);
		windows.put(id, window);
	}

	protected abstract void doRemoveWindow(LostWindowPacket lostWindowPacket, IXpraWindow window);

	protected abstract void doWindowMoveResize(WindowMoveResizePacket packet);

	protected abstract IXpraWindow createNewWindow(NewWindowPacket packet, IPacketSender iPacketSender);

	protected abstract void doClose();

	public void CloseAllWindows() {
		for (Entry<Integer, IXpraWindow> e : windows.entrySet()) {
			Packet sendPacket = new CloseWindowPacket(e.getKey());
			sendPacket(sendPacket, "close window packet");
		}
	}
}
