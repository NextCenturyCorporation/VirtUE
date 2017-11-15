package com.ncc.savior.desktop.xpra.application.javafx;

import java.io.Closeable;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.xpra.XpraClient;
import com.ncc.savior.desktop.xpra.application.XpraApplication;
import com.ncc.savior.desktop.xpra.application.XpraWindowManager;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.Window;
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

	private int x;

	private int y;

	public JavaFxApplication(XpraClient client, int x, int y, int initialWidth, int initialHeight, int baseWindowId) {
		super(client, baseWindowId);
		this.windowManager = new JavaFxXpraWindowManager(client, baseWindowId);
		this.width = initialWidth;
		this.height = initialHeight;
		this.x = x;
		this.y = y;

	}

	protected void initXpraWindowManager(int width, int height) {
		Group root = new Group();
		this.anchor = new AnchorPane();
		root.getChildren().add(anchor);
		this.scene = new Scene(root, width, height);
		this.applicationPacketHandler = new JavaFxXpraPacketHandler(scene);
		client.addPacketListener(applicationPacketHandler);
		JavaFxXpraWindowManager wm = (JavaFxXpraWindowManager) windowManager;

		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				stage.setScene(scene);
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
				stage.iconifiedProperty().addListener(new ChangeListener<Boolean>() {
					@Override
					public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue,
							Boolean newValue) {
						if (newValue) {
							onMinimized();
						} else {
							onRestored((int) 0, (int) 0, (int) scene.getWidth(), (int) scene.getHeight());
						}
					}
				});
				scene.widthProperty().addListener(new ChangeListener<Number>() {
					@Override
					public void changed(ObservableValue<? extends Number> observable, Number oldV, Number newV) {
						onSceneSizeChange(newV.intValue(), (int) scene.getHeight());
					}
				});
				scene.heightProperty().addListener(new ChangeListener<Number>() {
					@Override
					public void changed(ObservableValue<? extends Number> observable, Number oldV, Number newV) {
						onSceneSizeChange((int) scene.getWidth(), newV.intValue());
					}
				});
				stage.setX(x);
				stage.setY(y);
			}
		});
	}

	protected void onSceneSizeChange(int width, int height) {
		windowManager.resizeWindow(baseWindowId, width, height);
		onSizeChange(width, height);
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

	@Override
	public String toString() {
		return "JavaFxApplication [anchor=" + anchor + ", stage=" + stage + ", scene=" + scene + ", height=" + height
				+ ", width=" + width + ", show=" + show + ", applicationPacketHandler=" + applicationPacketHandler
				+ ", windowManager=" + windowManager + ", baseWindowId=" + baseWindowId + "]";
	}

	public Window getStage() {
		return stage;
	}

	@Override
	public void minimize() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				stage.setIconified(true);
			}
		});
	}

	@Override
	public void restore() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				stage.setIconified(false);
			}
		});
	}
}
