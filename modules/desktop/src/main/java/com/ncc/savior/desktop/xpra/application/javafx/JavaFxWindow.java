package com.ncc.savior.desktop.xpra.application.javafx;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import com.ncc.savior.desktop.xpra.application.BaseXpraWindow;
import com.ncc.savior.desktop.xpra.protocol.IPacketSender;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.DrawPacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.NewWindowPacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.WindowMoveResizePacket;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

public class JavaFxWindow extends BaseXpraWindow {

	private Canvas canvas;
	private List<String> modifiers;

	public JavaFxWindow(NewWindowPacket packet, IPacketSender packetSender, Canvas canvas) {
		super(packet, packetSender);
		modifiers = new ArrayList<String>(0);
		modifiers.add("shift");
		this.canvas = canvas;
		this.canvas.setOnMouseMoved(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				onMouseMove((int) event.getSceneX(), (int) event.getSceneY(), modifiers);
			}
		});
		this.canvas.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				onMousePress(event.getButton().ordinal(), (int) event.getSceneX(), (int) event.getSceneY(), modifiers);
			}
		});
		this.canvas.setOnMouseReleased(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				onMouseRelease(event.getButton().ordinal(), (int) event.getSceneX(), (int) event.getSceneY(),
						modifiers);
			}
		});
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	@Override
	public void draw(DrawPacket packet) {
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				try {
					Image img = new Image(new ByteArrayInputStream(packet.getData()));
					GraphicsContext g = canvas.getGraphicsContext2D();
					g.drawImage(img, packet.getX(), packet.getY(), packet.getWidth(), packet.getHeight());

					if (debugOutput) {
						g.setStroke(Color.BLUE);
						g.setFill(Color.GREEN);
						g.setLineWidth(2);
						g.strokeRect(1, 1, canvas.getWidth() - 2, canvas.getHeight() - 2);
						g.fillText("ID: " + id, 2, 10);
					}

					sendDamageSequence(packet);

				} catch (Exception e) {
					throw new RuntimeException("Failed decoding image: " + packet.getEncoding(), e);
				}
			}
		});

	}

	@Override
	public void onWindowMoveResize(WindowMoveResizePacket packet) {
		// TODO Auto-generated method stub

	}

	public Canvas getCanvas() {
		return canvas;
	}

}
