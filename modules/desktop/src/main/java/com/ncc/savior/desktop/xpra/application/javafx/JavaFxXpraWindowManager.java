package com.ncc.savior.desktop.xpra.application.javafx;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.xpra.XpraClient;
import com.ncc.savior.desktop.xpra.application.IXpraWindow;
import com.ncc.savior.desktop.xpra.application.XpraWindowManager;
import com.ncc.savior.desktop.xpra.protocol.IPacketSender;
import com.ncc.savior.desktop.xpra.protocol.keyboard.IKeyboard;
import com.ncc.savior.desktop.xpra.protocol.keyboard.JavaFxKeyboard;
import com.ncc.savior.desktop.xpra.protocol.keyboard.KeyCodeDto;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.LostWindowPacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.MapWindowPacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.NewWindowOverrideRedirectPacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.NewWindowPacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.WindowMoveResizePacket;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

/**
 * This class manages all the {@link JavaFxWindow} that need to be displayed in
 * a single {@link JavaFxApplication}. A window is defined by the Xpra protocol
 * and is considered any panel or screen that appears overtop another view. For
 * example, tooltips, history, or settings panels are often their own window
 * inside a single application.
 *
 *
 */
public class JavaFxXpraWindowManager extends XpraWindowManager {
	private static final Logger logger = LoggerFactory.getLogger(JavaFxXpraWindowManager.class);

	protected AnchorPane pane;
	protected Stage stage;
	protected JavaFxKeyboard keyboard;

	public JavaFxXpraWindowManager(XpraClient client, int baseWindowId) {
		super(client, baseWindowId);
		IKeyboard kb = client.getKeyboard();
		if (kb instanceof JavaFxKeyboard) {
			this.keyboard = (JavaFxKeyboard) kb;
		} else {
			logger.error("Error attempting to set keyboard.  Keyboard is wrong class.  Class="
					+ kb.getClass().getCanonicalName());
		}
	}

	@Override
	protected void doWindowMoveResize(WindowMoveResizePacket packet) {
		logger.warn("Window resize not implemented.  Packet=" + packet);
	}

	@Override
	protected IXpraWindow createNewWindow(NewWindowPacket packet, IPacketSender packetSender) {
		JavaFxWindow window = new JavaFxWindow(packet, packetSender, client.getKeyboard(), /* IFocusNotifier */this);

		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				Canvas canvas = new Canvas();
				window.initJavaFx(canvas, stage);
				canvas.setWidth(packet.getWidth());
				canvas.setHeight(packet.getHeight());
				pane.getChildren().add(canvas);
				double x = packet.getX();
				double y = packet.getY();

				AnchorPane.setTopAnchor(canvas, y);
				AnchorPane.setLeftAnchor(canvas, x);
			}
		});
		try {
			if (!(packet instanceof NewWindowOverrideRedirectPacket)) {
				packet.overrideXy(0, 0);
			}
			packetSender.sendPacket(new MapWindowPacket(packet));
		} catch (IOException e) {
			logger.error("Error sending MapWindowPacket. Packet=" + packet);
		}
		return window;
	}

	@Override
	protected void doRemoveWindow(LostWindowPacket lostWindowPacket, IXpraWindow window) {
		final AnchorPane myPane = pane;
		if (myPane != null) {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					JavaFxWindow w = (JavaFxWindow) window;
					Canvas canvas = w.getCanvas();
					myPane.getChildren().remove(canvas);
				}
			});
		}
	}

	public void setStage(Stage stage) {
		this.stage = stage;
		initStage();
		setGraphicsInit();
	}

	private void initStage() {
		this.stage.getScene().setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				KeyCodeDto keycode = keyboard.getKeyCodeFromEvent(event);
				// int key = event.getCode().ordinal();
				// String u = keyMap.getUnicodeName(key);
				// int c = keyMap.getKeyCode(key);
				// List<String> mods = JavaFxUtils.getModifiers(event);
				if (keycode != null) {
					onKeyDown(keycode, keyboard.getModifiers(event));
				}
			}
		});

		this.stage.getScene().setOnKeyReleased(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				KeyCodeDto keycode = keyboard.getKeyCodeFromEvent(event);
				// int key = event.getCode().ordinal();
				// String u = keyMap.getUnicodeName(key);
				// int c = keyMap.getKeyCode(key);
				// List<String> mods = JavaFxUtils.getModifiers(event);
				if (keycode != null) {
					onKeyUp(keycode, keyboard.getModifiers(event));
				}
			}
		});
	}

	public void setAnchor(AnchorPane anchor) {
		this.pane = anchor;
	}

	@Override
	protected void doClose() {
		this.pane = null;
		final Stage myStage = JavaFxXpraWindowManager.this.stage;
		JavaFxXpraWindowManager.this.stage = null;
		if (myStage != null) {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					myStage.close();
				}
			});
		}
	}
}
