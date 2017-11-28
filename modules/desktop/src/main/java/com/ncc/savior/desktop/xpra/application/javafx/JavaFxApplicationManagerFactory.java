package com.ncc.savior.desktop.xpra.application.javafx;

import com.ncc.savior.desktop.xpra.IApplicationManagerFactory;
import com.ncc.savior.desktop.xpra.XpraClient;
import com.ncc.savior.desktop.xpra.application.XpraApplicationManager;
import com.ncc.savior.desktop.xpra.protocol.keyboard.JavaFxKeyboard;

public class JavaFxApplicationManagerFactory implements IApplicationManagerFactory {

	private JavaFxKeyboard keyboard;

	public JavaFxApplicationManagerFactory(JavaFxKeyboard keyboard) {
		this.keyboard = keyboard;
	}

	@Override
	public XpraApplicationManager getApplicationManager(XpraClient client) {
		return new JavaFxApplicationManager(client, keyboard);
	}

}
