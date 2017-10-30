package com.ncc.savior.desktop.xpra.application.javafx;

import java.io.Closeable;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.xpra.XpraClient;
import com.ncc.savior.desktop.xpra.application.XpraApplication;
import com.ncc.savior.desktop.xpra.application.XpraWindowManager;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * Controls a JavaFX Application. An Application is defined as window that has
 * its own taskbar item. For example, each firefox window is considered an
 * application.
 *
 *
 */
public class JavaFxApplication extends XpraApplication implements Closeable {
	private static final Logger logger = LoggerFactory.getLogger(JavaFxApplication.class);

	private AnchorPane anchor;
	private Stage stage;
	private Scene scene;
	private int height;
	private int width;
	private boolean show;
	private JavaFxXpraPacketHandler applicationPacketHandler;

	public JavaFxApplication(XpraClient client, int initialWidth, int initialHeight, int baseWindowId) {
		super(client, baseWindowId);
		this.windowManager = new JavaFxXpraWindowManager(client, baseWindowId);
		this.width = initialWidth;
		this.height = initialHeight;

	}

	protected void initXpraWindowManager(int x, int y) {
		Group root = new Group();
		this.anchor = new AnchorPane();
		root.getChildren().add(anchor);
		this.scene = new Scene(root, x, y);
		this.applicationPacketHandler = new JavaFxXpraPacketHandler(scene);
		client.addPacketListener(applicationPacketHandler);
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				stage.setScene(scene);
				JavaFxXpraWindowManager wm = (JavaFxXpraWindowManager) windowManager;
				wm.setStage(stage);
				wm.setAnchor(anchor);
				stage.setOnCloseRequest(new EventHandler<WindowEvent>() {

					@Override
					public void handle(WindowEvent event) {
						try {
							JavaFxApplication.this.close();
						} catch (IOException e) {
							logger.error("Error attempting to close application." + JavaFxApplication.this);
						}
						XpraWindowManager manager = JavaFxApplication.super.windowManager;
						manager.CloseAllWindows();
					}
				});
			}
		});
	}

	public void setStage(Stage stage) {
		this.stage = stage;
		initXpraWindowManager(width, height);
		if (show) {
			stage.show();
		}
	}

	@Override
	public void Show() {
		show = true;
		if (stage != null) {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					stage.show();
				}
			});
		}
	}

	@Override
	public void doClose() throws IOException {
		if (applicationPacketHandler != null) {
			client.removePacketListener(applicationPacketHandler);
		}
		stage.close();
	}
}
