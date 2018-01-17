package com.ncc.savior.desktop.xpra.application.swing;

import java.awt.Color;

import com.ncc.savior.desktop.sidebar.RgbColor;
import com.ncc.savior.desktop.xpra.IApplicationManagerFactory;
import com.ncc.savior.desktop.xpra.XpraClient;
import com.ncc.savior.desktop.xpra.application.XpraApplicationManager;
import com.ncc.savior.desktop.xpra.protocol.keyboard.SwingKeyboard;


public class SwingApplicationManagerFactory implements IApplicationManagerFactory {

	private SwingKeyboard keyboard;

	public SwingApplicationManagerFactory(SwingKeyboard keyboard) {
		this.keyboard = keyboard;

	}

	@Override
	public XpraApplicationManager getApplicationManager(XpraClient client, RgbColor color) {
		SwingApplicationManager appManager = new SwingApplicationManager(client, keyboard);
		if (color != null) {
			Color c = new Color((float)color.getRed(), (float)color.getGreen(),(float)color.getBlue(),(float)color.getOpacity());
			appManager.setColor(c);
		}
		return appManager;
	}
}
