package com.ncc.savior.desktop.xpra.application.javafx;

import com.ncc.savior.desktop.xpra.XpraClient;
import com.ncc.savior.desktop.xpra.application.XpraApplication;
import com.ncc.savior.desktop.xpra.application.XpraApplicationManager;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.NewWindowPacket;

import javafx.application.Platform;
import javafx.stage.Stage;

public class JavaFxApplicationManager extends XpraApplicationManager {

	private Stage stage;

	public JavaFxApplicationManager(XpraClient client, Stage stage) {
		super(client);
		this.stage = stage;
		client.setKeyMap(keyMap);
	}

	@Override
	protected synchronized XpraApplication  createXpraApplication(NewWindowPacket packet) {
		int w = packet.getWidth();
		int h = packet.getHeight();
		JavaFxApplication app = new JavaFxApplication(client, w, h, packet.getWindowId());
		if (stage == null) {
			Platform.runLater(new Runnable() {

				@Override
				public void run() {
					app.setStage(new Stage());
				}
			});
		} else {
			app.setStage(stage);
			stage = null;
		}
		return app;
	}
}
