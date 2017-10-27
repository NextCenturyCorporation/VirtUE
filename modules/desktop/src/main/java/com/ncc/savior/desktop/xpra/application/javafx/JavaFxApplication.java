package com.ncc.savior.desktop.xpra.application.javafx;

import com.ncc.savior.desktop.xpra.XpraClient;
import com.ncc.savior.desktop.xpra.application.XpraApplication;

import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class JavaFxApplication extends XpraApplication {

	private AnchorPane anchor;
	private Stage stage;
	private Scene scene;
	private int height;
	private int width;
	private boolean show;

	public JavaFxApplication(XpraClient client, int initialWidth, int initialHeight, int baseWindowId) {
		super(client, baseWindowId);
		this.windowManager = new JavaFxXpraWindowManager(client);
		this.width = initialWidth;
		this.height = initialHeight;
	}

	protected void initXpraWindowManager(int x, int y) {
		Group root = new Group();
		this.anchor = new AnchorPane();
		root.getChildren().add(anchor);
		this.scene = new Scene(root, x, y);
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				stage.setScene(scene);
				JavaFxXpraWindowManager wm = (JavaFxXpraWindowManager) windowManager;
				wm.setStage(stage);
				wm.setAnchor(anchor);
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
}
