package com.ncc.savior.desktop.xpra.application.swing;

import java.awt.Color;
import java.awt.Container;
import java.awt.event.MouseAdapter;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.xpra.XpraClient;
import com.ncc.savior.desktop.xpra.application.IXpraWindow;
import com.ncc.savior.desktop.xpra.application.XpraWindowManager;
import com.ncc.savior.desktop.xpra.protocol.IPacketSender;
import com.ncc.savior.desktop.xpra.protocol.keyboard.IKeyboard;
import com.ncc.savior.desktop.xpra.protocol.keyboard.SwingKeyboard;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.LostWindowPacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.MapWindowPacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.NewWindowOverrideRedirectPacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.NewWindowPacket;





/**
 * This class manages all the {@link SwingWindow} that need to be displayed in a
 * single {@link SwingApplication}. A window is defined by the Xpra protocol and
 * is considered any panel or screen that appears overtop another view. For
 * example, tooltips, history, or settings panels are often their own window
 * inside a single application.
 *
 *
 */
public class SwingXpraWindowManager extends XpraWindowManager {
	private static final Logger logger = LoggerFactory.getLogger(SwingXpraWindowManager.class);

	protected Container pane;
	protected JFrame frame;
	protected SwingKeyboard keyboard;

	private int insetWidth;

	private int titleBarHeight;

	private Color color;

	private MouseAdapter mouseAdapter;

	public SwingXpraWindowManager(XpraClient client, int baseWindowId) {
		super(client, baseWindowId);
		IKeyboard kb = client.getKeyboard();
		if (kb instanceof SwingKeyboard) {
			this.keyboard = (SwingKeyboard) kb;
		} else {
			logger.error("Error attempting to set keyboard.  Keyboard is wrong class.  Class="
					+ kb.getClass().getCanonicalName());
		}
	}

	// @Override
	// protected void doWindowMoveResize(WindowMoveResizePacket packet) {
	// logger.warn("Window resize not implemented. Packet=" + packet);
	// }

	@Override
	protected IXpraWindow createNewWindow(NewWindowPacket packet, IPacketSender packetSender) {
		SwingWindow window = new SwingWindow(packet, packetSender, client.getKeyboard(), /* IFocusNotifier */this);
		window.setColor(color);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				JCanvas canvas = new JCanvas(packet.getWidth(), packet.getHeight());
				canvas.addMouseListener(mouseAdapter);
				canvas.addMouseMotionListener(mouseAdapter);

				window.initSwing(canvas, frame);
				pane.add(canvas);
				double x = packet.getX() - frame.getX();
				double y = packet.getY() - frame.getY();
				if (packet instanceof NewWindowOverrideRedirectPacket) {
					x -= insetWidth;
					y -= titleBarHeight;
				}
				if (packet.getMetadata().getFullscreen()) {
					x = 0;
					y = 0;
				}
				// double x = packet.getX();
				// double y = packet.getY();
				logger.warn("Setting canvas location not implemented");
				// AnchorPane.setTopAnchor(canvas, y);
				// AnchorPane.setLeftAnchor(canvas, x);
			}
		});
		try {
			packetSender.sendPacket(new MapWindowPacket(packet));
		} catch (IOException e) {
			logger.error("Error sending MapWindowPacket. Packet=" + packet);
		}
		return window;
	}

	@Override
	protected void doRemoveWindow(LostWindowPacket lostWindowPacket, IXpraWindow window) {
		final Container myComponent = pane;
		if (myComponent != null) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					SwingWindow w = (SwingWindow) window;
					JCanvas canvas = w.getCanvas();
					myComponent.remove(canvas);
				}
			});
		}
	}

	public void setFrame(JFrame frame) {
		this.frame = frame;
		initStage();
		setGraphicsInit();
	}

	public void setContainer(Container container) {
		this.pane = container;

	}

	private void initStage() {
		logger.warn("Keypress not implemented!");
		// this.frame.getScene().setOnKeyPressed(new EventHandler<KeyEvent>() {
		// @Override
		// public void handle(KeyEvent event) {
		// KeyCodeDto keycode = keyboard.getKeyCodeFromEvent(event);
		// // int key = event.getCode().ordinal();
		// // String u = keyMap.getUnicodeName(key);
		// // int c = keyMap.getKeyCode(key);
		// // List<String> mods = JavaFxUtils.getModifiers(event);
		// if (keycode != null) {
		// onKeyDown(keycode, keyboard.getModifiers(event));
		// }
		// }
		// });
		//
		// this.frame.getScene().setOnKeyReleased(new EventHandler<KeyEvent>() {
		// @Override
		// public void handle(KeyEvent event) {
		// KeyCodeDto keycode = keyboard.getKeyCodeFromEvent(event);
		// // int key = event.getCode().ordinal();
		// // String u = keyMap.getUnicodeName(key);
		// // int c = keyMap.getKeyCode(key);
		// // List<String> mods = JavaFxUtils.getModifiers(event);
		// if (keycode != null) {
		// onKeyUp(keycode, keyboard.getModifiers(event));
		// }
		// }
		// });
	}

	@Override
	protected void doClose() {
		// this.pane = null;
		final JFrame myFrame = SwingXpraWindowManager.this.frame;
		SwingXpraWindowManager.this.frame = null;
		if (myFrame != null) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					myFrame.setVisible(false);
					myFrame.dispose();
				}
			});
		}
	}

	public void setInsetWith(int insetWidth) {
		this.insetWidth = insetWidth;
	}

	public void setTitleBarHeight(int titleBarHeight) {
		this.titleBarHeight = titleBarHeight;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public void setMouseAdapter(MouseAdapter mouseAdapter) {
		this.mouseAdapter = mouseAdapter;
	}
}
