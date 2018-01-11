package com.ncc.savior.desktop.xpra.application.javafx;

import com.ncc.savior.desktop.sidebar.RgbColor;
import com.ncc.savior.desktop.xpra.IApplicationManagerFactory;
import com.ncc.savior.desktop.xpra.XpraClient;
import com.ncc.savior.desktop.xpra.application.XpraApplicationManager;
import com.ncc.savior.desktop.xpra.protocol.keyboard.JavaFxKeyboard;

import javafx.scene.paint.Color;

public class JavaFxApplicationManagerFactory implements IApplicationManagerFactory {

	private JavaFxKeyboard keyboard;

	public JavaFxApplicationManagerFactory(JavaFxKeyboard keyboard) {
		this.keyboard = keyboard;

	}

	@Override
	public XpraApplicationManager getApplicationManager(XpraClient client, RgbColor color) {
		JavaFxApplicationManager appManager = new JavaFxApplicationManager(client, keyboard);
		if (color != null) {
			appManager.setColor(Color.color(color.getRed(), color.getGreen(), color.getBlue(), color.getOpacity()));
		}
		return appManager;
	}
}
