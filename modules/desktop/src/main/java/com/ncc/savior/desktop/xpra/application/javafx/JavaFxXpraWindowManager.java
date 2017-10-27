package com.ncc.savior.desktop.xpra.application.javafx;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.xpra.XpraClient;
import com.ncc.savior.desktop.xpra.application.IXpraWindow;
import com.ncc.savior.desktop.xpra.application.XpraWindowManager;
import com.ncc.savior.desktop.xpra.protocol.IPacketSender;
import com.ncc.savior.desktop.xpra.protocol.keyboard.IKeyMap;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.LostWindowPacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.MapWindowPacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.NewWindowPacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.WindowMoveResizePacket;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class JavaFxXpraWindowManager extends XpraWindowManager {
	private static final Logger logger = LoggerFactory.getLogger(JavaFxXpraWindowManager.class);

	protected AnchorPane pane;
	protected Stage stage;
	protected IKeyMap keyMap;



	public JavaFxXpraWindowManager(XpraClient client, Stage primaryStage, AnchorPane anchor) {
		super(client);
		this.pane = anchor;
		this.stage = primaryStage;
		this.keyMap = client.getKeyMap();
		this.stage.getScene().setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				int key = event.getCode().ordinal();
				String u = keyMap.getUnicodeName(key);
				int c = keyMap.getKeyCode(key);
				List<String> mods = JavaFxUtils.getModifiers(event);
				onKeyDown(0, c, u, mods);
			}
		});

		this.stage.getScene().setOnKeyReleased(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				int key = event.getCode().ordinal();
				String u = keyMap.getUnicodeName(key);
				int c = keyMap.getKeyCode(key);
				List<String> mods = JavaFxUtils.getModifiers(event);
				onKeyUp(0, c, u, mods);

			}
		});
	}

	@Override
	protected void doWindowMoveResize(WindowMoveResizePacket packet) {
		logger.warn("Window resize not implemented.  Packet=" + packet);

	}

	@Override
	protected IXpraWindow createNewWindow(NewWindowPacket packet, IPacketSender packetSender) {
		Canvas canvas = new Canvas();
		JavaFxWindow window = new JavaFxWindow(packet, packetSender, canvas, stage, client.getKeyMap(),
				/* IFocusNotifier */this);

		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				canvas.setWidth(packet.getWidth());
				canvas.setHeight(packet.getHeight());
				pane.getChildren().add(canvas);
				AnchorPane.setTopAnchor(canvas, (double) packet.getY());
				AnchorPane.setLeftAnchor(canvas, (double) packet.getX());
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
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				pane.getChildren().remove(((JavaFxWindow) window).getCanvas());
			}
		});

	}


}
