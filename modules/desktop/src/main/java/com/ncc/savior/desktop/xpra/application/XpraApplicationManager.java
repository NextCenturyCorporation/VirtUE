package com.ncc.savior.desktop.xpra.application;

import java.awt.Component;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

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
import com.ncc.savior.desktop.dnd.messages.DndDataResponseMessage;
import com.ncc.savior.desktop.dnd.messages.DndStartDragMessage;
import com.ncc.savior.desktop.xpra.XpraClient;
import com.ncc.savior.desktop.xpra.application.swing.JCanvas;
import com.ncc.savior.desktop.xpra.protocol.IPacketHandler;
import com.ncc.savior.desktop.xpra.protocol.packet.DisconnectPacket;
import com.ncc.savior.desktop.xpra.protocol.packet.PacketType;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.ConfigureOverrideRedirectPacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.InitiateMoveResizePacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.LostWindowPacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.MouseButtonActionPacket;
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
import com.ncc.savior.util.JavaUtil;

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
	protected DataFlavor[] dragFlavors;
	protected Map<String, CompletableFuture> dragFutures;
	protected volatile DndDataResponseMessage dataMessage;

	public XpraApplicationManager(XpraClient client) {
		this.clientIsDragging = false;
		this.dragFutures = Collections.synchronizedMap(new HashMap<String, CompletableFuture>());
		this.client = client;
		this.applications = new HashMap<Integer, XpraApplication>();
		this.windowIdsToApplications = new HashMap<Integer, XpraApplication>();
		initPacketHandling();
		// TestTransferHandler globalTransferHandler = new TestTransferHandler(-1);
		// TransferHandler globalTransferHandler = new TransferHandler("text");
		// globalTransferHandler.setValue("test");
		TransferHandlerFactory transferHandlerFactory = new TransferHandlerFactory();
		dndHandler = new IDndDragHandler() {

			@Override
			public void onDragLeave(int x, int y, List<String> modifiers, int windowId) {
				for (Integer destId : hiddenWindowIds) {
					Packet packet = new MousePointerPositionPacket(destId, x, y, modifiers);
					try {
						client.getPacketSender().sendPacket(packet);
						logger.debug("sent packet1 " + packet);
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
			public TransferHandlerFactory getTransferHandlerFactory() {
				return transferHandlerFactory;
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
					logger.debug("got import request");
					DndCanImportRequestMessage m = (DndCanImportRequestMessage) message;
					String cci = client.getClipboardClientId();
					if (m.getSourceId().equals(cci)) {
						clientIsDragging = true;
						dragFlavors = m.getFlavors();
						respondToCanImportRequest(client, message, m, m.getRequestId());
					}
				} else if (message instanceof DndStartDragMessage) {
					DndStartDragMessage m = (DndStartDragMessage) message;

					String cci = client.getClipboardClientId();

					if (m.getSourceId().equals(cci)) {
						dragFlavors = m.getFlavors();
						logger.debug("got drag start request");
						// message form OUR drag and drop client so this is relevant to us
						clientIsDragging = true;

						respondToCanImportRequest(client, message, m, m.getRequestId());
					}
				} else if (message instanceof DndDataResponseMessage) {
					DndDataResponseMessage m = (DndDataResponseMessage) message;
					logger.debug("got data and setting data message to " + m);
					dataMessage = m;
					// CompletableFuture f = dragFutures.remove(m.getRequestId());
					// if (f != null) {
					// f.complete(m.getData());
					// }
					// TODO end the drag/drop action
				}

			}

			private void respondToCanImportRequest(XpraClient client, IClipboardMessage message, IClipboardMessage m,
					String requestId) {
				String destId = message.getSourceId();
				IClipboardMessageSenderReceiver transmitter = client.getClipboardTransmitter();
				boolean allowTransfer = transmitter != null && transmitter.isValid();
				if (allowTransfer) {
					DndCanImportResponseMessage response = new DndCanImportResponseMessage("hubId", destId, requestId,
							true);
					try {
						transmitter.sendMessageToHub(response);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
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

	public class TransferHandlerFactory {
		public TestTransferHandler getTransferHandler(int windowId) {
			return new TestTransferHandler(windowId);
		}
	}

	public class TestTransferHandler extends TransferHandler {
		private static final long serialVersionUID = 1L;
		public final DataFlavor SUPPORTED_DATA_FLAVOR = DataFlavor.stringFlavor;
		private String value;
		private int windowId;
		private Thread thread;
		private boolean terminateThread;

		TestTransferHandler(int windowId) {
			this.windowId = windowId;
		}

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
		public void exportAsDrag(JComponent comp, InputEvent e, int action) {
			onDragStart();
			super.exportAsDrag(comp, e, action);
		}

		@Override
		protected Transferable createTransferable(JComponent c) {
			logger.debug("creating transferable");
			Transferable t = new Transferable() {

				@Override
				public boolean isDataFlavorSupported(DataFlavor flavor) {
					return flavor.equals(SUPPORTED_DATA_FLAVOR);
					// for (DataFlavor supportedFlavor:dragFlavors) {
					// if (flavor.equals(supportedFlavor)) {
					// return true;
					// }
					// }
					// return false;
				}

				@Override
				public DataFlavor[] getTransferDataFlavors() {
					return new DataFlavor[] { SUPPORTED_DATA_FLAVOR };
				}

				@Override
				public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
					try {
						logger.debug("trying to get transfer data");
						for (Integer id : hiddenWindowIds) {
							Point p = MouseInfo.getPointerInfo().getLocation();
							int x = (int) p.getX();
							int y = (int) p.getY();
							MousePointerPositionPacket p1 = new MousePointerPositionPacket(id, x, y);
							MouseButtonActionPacket p2 = new MouseButtonActionPacket(id, 1, false, x, y);
							client.getPacketSender().sendPacket(p1);
							client.getPacketSender().sendPacket(p2);
							// logger.debug("send packets 2" + p1);
							// logger.debug("send packets 2" + p2);
						}
						// IClipboardMessageSenderReceiver transmitter =
						// client.getClipboardTransmitter();
						// String requestId = UUID.randomUUID().toString();
						// DndDataRequestMessage message = new DndDataRequestMessage("dndHub", flavor,
						// requestId);
						// transmitter.sendMessageToHub(message);
						// CompletableFuture<Object> completableFuture = new
						// CompletableFuture<Object>();
						// dragFutures.put(requestId, completableFuture);
						//
						// Object obj = completableFuture.get(2000, TimeUnit.MILLISECONDS);
						// logger.debug("got future response for clipboard data: " + obj);
						long timeoutTimeMillis = System.currentTimeMillis() + 2000;
						while (dataMessage == null) {
							if (timeoutTimeMillis < System.currentTimeMillis()) {
								throw new TimeoutException();
							}
							JavaUtil.sleepAndLogInterruption(5);
						}
						Object obj = dataMessage.getData();
						dataMessage = null;
						return obj;
					} catch (TimeoutException e) {
						logger.error("timeout with getting drag and drop data", e);
						return "timeout";
					} catch (Throwable t) {
						logger.error("Unexpected exception!", t);
						return "ERROR";
					}
				}
			};

			return t;
		}

		@Override
		public boolean canImport(TransferHandler.TransferSupport support) {
			// logger.debug("can import " +
			// support.getDropLocation().getDropPoint().getX());
			sendMousePosition(windowId, false);
			return true;
		}

		@Override
		protected void exportDone(JComponent source, Transferable data, int action) {
			onDragStop();
			super.exportDone(source, data, action);
			logger.debug("export done");
			// Decide what to do after the drop has been accepted
		}

		public boolean isMouseWithinComponent(Component c) {
			Point mousePos = MouseInfo.getPointerInfo().getLocation();
			Rectangle bounds = c.getBounds();
			bounds.setLocation(c.getLocationOnScreen());
			return bounds.contains(mousePos);
		}

		public void sendMousePosition(int targetWindowId, boolean includeMouseUp) {
			List<String> mod = new ArrayList<String>();
			Point loc = MouseInfo.getPointerInfo().getLocation();
			int x = loc.x;
			int y = loc.y;
			try {
				MousePointerPositionPacket packet = new MousePointerPositionPacket(targetWindowId, x, y, mod);
				client.getPacketSender().sendPacket(packet);
				// logger.debug("sent packet 3 " + packet);
				if (includeMouseUp) {
					MouseButtonActionPacket p;

					p = new MouseButtonActionPacket(windowId, 1, false, x, y);
					client.getPacketSender().sendPacket(p);

					// p = new MouseButtonActionPacket(targetWindowId, 1, false, x, y);
					// client.getPacketSender().sendPacket(p);
					// logger.debug("sent packet 3.2 " + p);

				}
			} catch (IOException e) {
				logger.error("Error sending mouse data via TransferHandler", e);
			}
		}

		private void onDragStart() {
			terminateThread = false;
			thread = new Thread(() -> {
				while (!terminateThread) {
					Iterator<Integer> itr = hiddenWindowIds.iterator();
					if (itr.hasNext()) {
						windowId = itr.next();
						sendMousePosition(windowId, false);
					}
					JavaUtil.sleepAndLogInterruption(500);
				}
				sendMousePosition(windowId, true);
			});

			thread.start();
		}

		private void onDragStop() {
			terminateThread = true;
		}
	}
}
