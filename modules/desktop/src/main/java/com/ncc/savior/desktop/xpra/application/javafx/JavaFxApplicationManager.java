package com.ncc.savior.desktop.xpra.application.javafx;

import com.ncc.savior.desktop.xpra.XpraClient;
import com.ncc.savior.desktop.xpra.application.XpraApplication;
import com.ncc.savior.desktop.xpra.application.XpraApplicationManager;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.NewWindowPacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.RaiseWindowPacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.WindowMetadata;

import javafx.application.Platform;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * JavaFX Application manager keeps track of and controls
 * {@link JavaFxApplication}s. An Application is defined as window that has its
 * own taskbar item. For example, each firefox window is considered an
 * application.
 *
 *
 */
public class JavaFxApplicationManager extends XpraApplicationManager {

	private Stage stage;

	public JavaFxApplicationManager(XpraClient client, Stage stage) {
		super(client);
		this.stage = stage;
		client.setKeyMap(keyMap);
	}

	@Override
	protected synchronized XpraApplication createXpraApplication(NewWindowPacket packet) {
		return createNewJavaFxXpraApplication(packet, null);
	}

	private XpraApplication createNewJavaFxXpraApplication(NewWindowPacket packet, JavaFxApplication parent) {
		int w = packet.getWidth();
		int h = packet.getHeight();
		JavaFxApplication app = new JavaFxApplication(client, w, h, packet.getWindowId());
		if (stage == null) {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					WindowMetadata meta = packet.getMetadata();
					boolean isModal = meta.getModal();
					StageStyle style = (isModal ? StageStyle.UTILITY : StageStyle.DECORATED);
					Stage stage = new Stage(style);
					if (isModal && parent != null) {
						stage.initModality(Modality.WINDOW_MODAL);
						stage.initOwner(parent.getStage());
						stage.setIconified(false);
					}
					app.setStage(stage);
				}
			});
		} else {
			app.setStage(stage);
			stage = null;
		}
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
}
