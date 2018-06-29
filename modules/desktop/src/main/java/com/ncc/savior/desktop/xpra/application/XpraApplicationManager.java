package com.ncc.savior.desktop.xpra.application;

import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.clipboard.IClipboardMessageHandler;
import com.ncc.savior.desktop.clipboard.IClipboardMessageSenderReceiver;
import com.ncc.savior.desktop.clipboard.messages.IClipboardMessage;
import com.ncc.savior.desktop.dnd.IDndDragHandler;
import com.ncc.savior.desktop.dnd.messages.DndCanImportRequestMessage;
import com.ncc.savior.desktop.dnd.messages.DndCanImportResponseMessage;
import com.ncc.savior.desktop.xpra.XpraClient;
import com.ncc.savior.desktop.xpra.application.swing.JCanvas;
import com.ncc.savior.desktop.xpra.protocol.IPacketHandler;
import com.ncc.savior.desktop.xpra.protocol.packet.DisconnectPacket;
import com.ncc.savior.desktop.xpra.protocol.packet.PacketType;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.ConfigureOverrideRedirectPacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.InitiateMoveResizePacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.LostWindowPacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.MousePointerPositionPacket;
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
 *
 */
public abstract class XpraApplicationManager {
	private static final Logger logger = LoggerFactory.getLogger(XpraApplicationManager.class);
	protected XpraClient client;
	protected Map<Integer, XpraApplication> applications;
	protected Map<Integer, XpraApplication> windowIdsToApplications;
	private boolean show = false;
	private boolean setDebugOutput;
	private Set<Integer> hiddenWindowIds = new HashSet<Integer>();
	protected IDndDragHandler dndHandler;
	protected volatile boolean clientIsDragging;

	public XpraApplicationManager(XpraClient client) {
		this.clientIsDragging = false;
		this.client = client;
		this.applications = new HashMap<Integer, XpraApplication>();
		this.windowIdsToApplications = new HashMap<Integer, XpraApplication>();
		initPacketHandling();
		TestTransferHandler globalTransferHandler = new TestTransferHandler();
		// TransferHandler globalTransferHandler = new TransferHandler("text");
		globalTransferHandler.setValue("test");
		dndHandler = new IDndDragHandler() {

			@Override
			public void onDragLeave(int x, int y, List<String> modifiers, int windowId) {
				for (Integer destId : hiddenWindowIds) {
					Packet packet = new MousePointerPositionPacket(destId, x, y, modifiers);
					try {
						client.getPacketSender().sendPacket(packet);
					} catch (IOException e) {
						logger.error("error sending packet to register possible drag", e);
					}
				}
			}

			@Override
			public void onDragEnter(int x, int y, List<String> modifiers, int id) {
				logger.debug("DRAG ENTER event not implemented yet");

			}

			@Override
			public void onMouseDrag(MouseEvent event) {
				Component c = event.getComponent();
				if (c instanceof JCanvas) {
					if (clientIsDragging) {
						JCanvas canvas = (JCanvas) c;
						TransferHandler th = canvas.getTransferHandler();
						logger.debug("export as drag!");
						th.exportAsDrag(canvas, event, TransferHandler.COPY);
					}
				}
			}

			@Override
			public TransferHandler getTransferHandler() {
				return globalTransferHandler;
			}
		};

		IClipboardMessageHandler dndMessageHandler = new IClipboardMessageHandler() {

			@Override
			public void onMessageError(String description, IOException e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onMessage(IClipboardMessage message, String groupId) {
				if (message instanceof DndCanImportRequestMessage) {
					DndCanImportRequestMessage m = (DndCanImportRequestMessage) message;
					String cci = client.getClipboardClientId();
					IClipboardMessageSenderReceiver transmitter = client.getClipboardTransmitter();
					if (m.getSourceId().equals(cci)) {
						logger.debug("got can import request");
						// message form OUR drag and drop client so this is relevant to us
						clientIsDragging = true;
						String destId = message.getSourceId();
						boolean allowTransfer = transmitter != null && transmitter.isValid();
						if (allowTransfer) {
							DndCanImportResponseMessage response = new DndCanImportResponseMessage("hubId", destId,
									m.getRequestId(), false);
							try {
								logger.debug("sending response");
								transmitter.sendMessageToHub(response);
								logger.debug("sent response");
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				}

			}

			@Override
			public void closed() {
				// TODO Auto-generated method stub

			}
		};
		client.setDndMessageHandler(dndMessageHandler);
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
				if (packet instanceof WindowPacket) {
					if (hiddenWindowIds.contains(((WindowPacket) packet).getWindowId())) {
						return;
					}
				}
				switch (packet.getType()) {
				case NEW_WINDOW:
					if (packet instanceof NewWindowPacket) {
						NewWindowPacket p = (NewWindowPacket) packet;
						if (p.getMetadata().getTitle().equals("DONOTSHOW")) {
							hiddenWindowIds.add(p.getWindowId());
						}
					}
					// if (packet instanceof WindowPacket && ignoreIds.contains(((WindowPacket)
					// packet).getWindowId())) {
					// logger.debug("ignoring packet=" + packet);
					// } else {
					NewWindowPacket p = (NewWindowPacket) packet;
					int parentId = p.getMetadata().getParentId();
					// boolean isModal = p.getMetadata().getModal();
					if (parentId > 0 /* && isModal8 */) {
						XpraApplication parent = applications.get(parentId);
						onModal(p, parent);
					} else {
						onNewWindow(p, !hiddenWindowIds.contains(p.getWindowId()));
					}
					// }
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
		Boolean fullscreen = meta.getFullscreenOrNull();
		if (fullscreen != null) {
			if (fullscreen) {
				app.fullscreen();
			} else {
				app.notFullScreen();
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

	private void onNewWindow(NewWindowPacket packet, boolean showable) {
		XpraApplication app = createXpraApplication(packet, showable);
		app.setDndHandler(dndHandler);
		if (show && showable) {
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

		XpraApplication app = createXpraApplication(packet, parent, true);
		app.setDndHandler(dndHandler);
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
			onNewWindow(packet, true);
		}
	}

	protected void onConfigureRedirectOverride(ConfigureOverrideRedirectPacket packet) {
		int wid = packet.getWindowId();
		XpraApplication app = applications.get(wid);
		if (app != null) {
			app.setLocationSize(packet.getX(), packet.getY(), packet.getWidth(), packet.getHeight());
		} else {
			logger.warn("Received packet with invalid application. packet=" + packet);
		}
	}

	protected void onWindowMoveResize(WindowMoveResizePacket packet) {
		int wid = packet.getWindowId();
		XpraApplication app = applications.get(wid);
		if (app != null) {
			app.setLocationSize(packet.getX(), packet.getY(), packet.getWidth(), packet.getHeight());
		} else {
			logger.warn("Received packet with invalid application. packet=" + packet);
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

	protected abstract XpraApplication createXpraApplication(NewWindowPacket packet, boolean showable);

	protected abstract XpraApplication createXpraApplication(NewWindowPacket packet, XpraApplication parent,
			boolean showable);

	public void closeAllWindows() {
		for (XpraApplication app : this.applications.values()) {
			try {
				app.close();
			} catch (IOException e) {
				logger.error("Error closing app=" + app.getBaseWindowId());
			}
		}
	}

	public class TestTransferHandler extends TransferHandler {
		private static final long serialVersionUID = 1L;
		public final DataFlavor SUPPORTED_DATE_FLAVOR = DataFlavor.stringFlavor;
		private String value;

		public void setValue(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}

		@Override
		public int getSourceActions(JComponent c) {
			// if (dragging) {
			return DnDConstants.ACTION_COPY_OR_MOVE;
			// } else {
			// return 0;
			// }
		}

		@Override
		protected Transferable createTransferable(JComponent c) {
			logger.debug("creating transferable");
			Transferable t = new StringSelection(getValue());
			return t;
		}

		@Override
		public boolean canImport(TransferHandler.TransferSupport support) {
			logger.debug("can import " + support.getDropLocation().getDropPoint().getX());
			return true;
		}

		@Override
		protected void exportDone(JComponent source, Transferable data, int action) {
			super.exportDone(source, data, action);
			logger.debug("export done");
			// Decide what to do after the drop has been accepted
		}
	}
}
