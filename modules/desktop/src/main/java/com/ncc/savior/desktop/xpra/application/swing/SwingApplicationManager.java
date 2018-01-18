package com.ncc.savior.desktop.xpra.application.swing;

import java.awt.Color;

import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.xpra.XpraClient;
import com.ncc.savior.desktop.xpra.application.XpraApplication;
import com.ncc.savior.desktop.xpra.application.XpraApplicationManager;
import com.ncc.savior.desktop.xpra.application.javafx.JavaFxApplication;
import com.ncc.savior.desktop.xpra.protocol.keyboard.SwingKeyboard;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.NewWindowPacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.RaiseWindowPacket;


/**
 * JavaFX Application manager keeps track of and controls
 * {@link JavaFxApplication}s. An Application is defined as window that has its
 * own taskbar item. For example, each firefox window is considered an
 * application.
 *
 *
 */
public class SwingApplicationManager extends XpraApplicationManager {
	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(SwingApplicationManager.class);
	private Color color;

	public SwingApplicationManager(XpraClient client, SwingKeyboard keyboard) {
		super(client);
		client.setKeyboard(keyboard);
	}

	@Override
	protected synchronized XpraApplication createXpraApplication(NewWindowPacket packet) {
		return createNewSwingXpraApplication(packet, null);
	}

	private XpraApplication createNewSwingXpraApplication(NewWindowPacket packet, SwingApplication parent) {
		SwingApplication app = new SwingApplication(client, packet, parent, color);
		return app;
	}

	@Override
	protected void onRaiseWindow(RaiseWindowPacket packet) {
		int id = packet.getWindowId();
		SwingApplication app = (SwingApplication) windowIdsToApplications.get(id);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				logger.debug("focusing window" + this.toString());
				app.getFrame().requestFocus();
			}
		});
	}

	@Override
	protected XpraApplication createXpraApplication(NewWindowPacket packet, XpraApplication parent) {
		SwingApplication p = null;
		if (parent instanceof SwingApplication) {
			p = (SwingApplication) parent;
		}
		return createNewSwingXpraApplication(packet, p);
	}

	public void setColor(Color color) {
		this.color = color;
	}
}
