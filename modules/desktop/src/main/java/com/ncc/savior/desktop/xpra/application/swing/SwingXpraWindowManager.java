package com.ncc.savior.desktop.xpra.application.swing;

import java.awt.Color;
import java.awt.Container;
import java.awt.Window;
import java.awt.Window.Type;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.io.IOException;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.xpra.XpraClient;
import com.ncc.savior.desktop.xpra.application.IXpraWindow;
import com.ncc.savior.desktop.xpra.application.XpraWindowManager;
import com.ncc.savior.desktop.xpra.protocol.IPacketSender;
import com.ncc.savior.desktop.xpra.protocol.keyboard.IKeyboard;
import com.ncc.savior.desktop.xpra.protocol.keyboard.KeyCodeDto;
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
	protected JFrame baseFrame;
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

				// int baseWindowId = SwingXpraWindowManager.this.baseWindowId;
				boolean isMainWindow = baseWindowId == packet.getWindowId();
				Window myFrame;
				Container myPane;
				if (isMainWindow) {
					myFrame = baseFrame;
					myPane = pane;
				} else {
					// myFrame = new JDialog(baseFrame);
					// myFrame.setType(Type.UTILITY);
					// ((JDialog)myFrame).setUndecorated(true);
					// myFrame.setSize(packet.getWidth(), packet.getHeight());
					// myPane = ((JDialog) myFrame).getContentPane();

					myFrame = new JFrame();
					myFrame.setType(Type.UTILITY);
					((JFrame) myFrame).setUndecorated(true);
					myFrame.setSize(packet.getWidth(), packet.getHeight());
					myPane = ((JFrame) myFrame).getContentPane();

					myPane.setSize(packet.getWidth(), packet.getHeight());
					myFrame.setLocation(packet.getX(), packet.getY());
					myFrame.setVisible(true);
				}
				if (myFrame instanceof JFrame) {
					window.initSwing(canvas, (JFrame) myFrame, null);
				} else if (myFrame instanceof JDialog) {
					window.initSwing(canvas, baseFrame, (JDialog) myFrame);
				}
				myPane.add(canvas);
				double x = packet.getX() - myFrame.getX();
				double y = packet.getY() - myFrame.getY();
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
		this.baseFrame = frame;
		initStage();
		setGraphicsInit();
	}

	public void setContainer(Container container) {
		this.pane = container;

	}

	private void initStage() {
		KeyAdapter keyAdapter = new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {

			}

			@Override
			public void keyReleased(KeyEvent e) {
				List<String> mods = SwingUtils.getModifiers(e);
				KeyCodeDto key = SwingUtils.getKeyCodeFromEvent(e, keyboard);
				onKeyUp(key, mods);
			}

			@Override
			public void keyPressed(KeyEvent e) {
				List<String> mods = SwingUtils.getModifiers(e);
				KeyCodeDto key = SwingUtils.getKeyCodeFromEvent(e, keyboard);
				logger.debug("Key: " + key + " mods=" + mods + " char=" + e.getKeyChar());
				if (key == null) {
					logger.warn("Key didn't work " + e.getKeyCode() + "-" + e.getKeyChar());
				}
				onKeyDown(key, mods);
			}
		};
		this.baseFrame.addKeyListener(keyAdapter);

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
		final JFrame myFrame = SwingXpraWindowManager.this.baseFrame;
		SwingXpraWindowManager.this.baseFrame = null;
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
