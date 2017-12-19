package com.ncc.savior.desktop.xpra.application.javafx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.xpra.XpraClient;
import com.ncc.savior.desktop.xpra.application.XpraApplication;
import com.ncc.savior.desktop.xpra.application.XpraApplicationManager;
import com.ncc.savior.desktop.xpra.protocol.keyboard.JavaFxKeyboard;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.NewWindowPacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.RaiseWindowPacket;

import javafx.application.Platform;
import javafx.scene.paint.Color;

/**
 * JavaFX Application manager keeps track of and controls
 * {@link JavaFxApplication}s. An Application is defined as window that has its
 * own taskbar item. For example, each firefox window is considered an
 * application.
 *
 *
 */
public class JavaFxApplicationManager extends XpraApplicationManager {
	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(JavaFxApplicationManager.class);
	private Color color;

	public JavaFxApplicationManager(XpraClient client, JavaFxKeyboard keyboard) {
		super(client);
		client.setKeyboard(keyboard);
	}

	@Override
	protected synchronized XpraApplication createXpraApplication(NewWindowPacket packet) {
		return createNewJavaFxXpraApplication(packet, null);
	}

	private XpraApplication createNewJavaFxXpraApplication(NewWindowPacket packet, JavaFxApplication parent) {
		JavaFxApplication app = new JavaFxApplication(client, packet, parent, color);
		return app;
	}

	@Override
	protected void onRaiseWindow(RaiseWindowPacket packet) {
		int id = packet.getWindowId();
		JavaFxApplication app = (JavaFxApplication) windowIdsToApplications.get(id);
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				app.getStage().requestFocus();
			}
		});
	}

	@Override
	protected XpraApplication createXpraApplication(NewWindowPacket packet, XpraApplication parent) {
		JavaFxApplication p = null;
		if (parent instanceof JavaFxApplication) {
			p = (JavaFxApplication) parent;
		}
		return createNewJavaFxXpraApplication(packet, p);
	}

	public void setColor(Color color) {
		this.color = color;
	}
}
