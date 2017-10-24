package com.ncc.savior.desktop.xpra.application.javafx;

import com.ncc.savior.desktop.xpra.XpraClient;
import com.ncc.savior.desktop.xpra.application.IXpraWindow;
import com.ncc.savior.desktop.xpra.application.XpraWindowManager;
import com.ncc.savior.desktop.xpra.protocol.IPacketSender;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.LostWindowPacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.NewWindowPacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.WindowMoveResizePacket;

import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.AnchorPane;

public class JavaFxXpraWindowManager extends XpraWindowManager {

	private AnchorPane pane;

	public JavaFxXpraWindowManager(XpraClient client, AnchorPane anchor) {
		super(client);
		this.pane = anchor;
	}

	@Override
	protected void doWindowMoveResize(WindowMoveResizePacket packet) {
		// TODO Auto-generated method stub

	}

	@Override
	protected IXpraWindow createNewWindow(NewWindowPacket packet, IPacketSender packetSender) {
		Canvas canvas = new Canvas();
		JavaFxWindow window = new JavaFxWindow(packet, packetSender, canvas);

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
