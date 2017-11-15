package com.ncc.savior.desktop.xpra.application.javafx;

import java.io.Closeable;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.xpra.XpraClient;
import com.ncc.savior.desktop.xpra.application.XpraApplication;
import com.ncc.savior.desktop.xpra.application.XpraWindowManager;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.InitiateMoveResizePacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.InitiateMoveResizePacket.MoveResizeDirection;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
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
	private boolean draggingApp = false;

	private InitiateMoveResizePacket initMoveResizePacket;

	private boolean resizeTop;

	private boolean resizeRight;

	private boolean resizeBottom;

	private boolean resizeLeft;
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
				scene.setOnMouseReleased(new EventHandler<MouseEvent>() {

					@Override
					public void handle(MouseEvent event) {
						if (isMoveResizing()) {
							// sendPacket(new ConfigureWindowPacket(id, (int) stage.getX(), (int)
							// stage.getY(),
							// (int) stage.getWidth(), (int) stage.getHeight()), "Configure Window");

							clearInitMoveResize();
						}
					}
				});

				scene.setOnMouseDragged(new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent event) {
						if (draggingApp) {
							int sceneXStart = initMoveResizePacket.getxRoot();
							int sceneYStart = initMoveResizePacket.getyRoot();
							stage.setX(event.getScreenX() - sceneXStart);
							stage.setY(event.getScreenY() - sceneYStart);
						}
						if (resizeTop) {
							double ydelta = stage.getY() - event.getScreenY();
							stage.setHeight(stage.getHeight() + ydelta);
							stage.setY(event.getScreenY());
						}
						if (resizeLeft) {
							double xdelta = stage.getX() - event.getScreenX();
							stage.setWidth(stage.getWidth() + xdelta);
							stage.setX(event.getScreenX());
						}
						if (resizeRight) {
							double width = event.getScreenX() - stage.getX();
							stage.setWidth(width);
						}
						if (resizeBottom) {
							double height = event.getScreenY() - stage.getY();
							stage.setHeight(height);
						}
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

	@Override
	public void initiateMoveResize(InitiateMoveResizePacket packet) {
		clearInitMoveResize();
		MoveResizeDirection dir = packet.getDirection();
		if (dir.equals(MoveResizeDirection.MOVERESIZE_MOVE)) {
			draggingApp = true;
			initMoveResizePacket = packet;
		}
		int dirInt = packet.getDirectionInt();
		// MOVERESIZE_SIZE_TOPLEFT = 0
		// MOVERESIZE_SIZE_TOP = 1
		// MOVERESIZE_SIZE_TOPRIGHT = 2
		// MOVERESIZE_SIZE_RIGHT = 3
		// MOVERESIZE_SIZE_BOTTOMRIGHT = 4
		// MOVERESIZE_SIZE_BOTTOM = 5
		// MOVERESIZE_SIZE_BOTTOMLEFT = 6
		// MOVERESIZE_SIZE_LEFT = 7
		if (dirInt >= 0 && dirInt <= 2) {
			// top
			resizeTop = true;
		}
		if (dirInt >= 2 && dirInt <= 4) {
			// right
			resizeRight = true;
		}
		if (dirInt >= 4 && dirInt <= 6) {
			// bottom
			resizeBottom = true;
		}
		if (dirInt == 0 || (dirInt >= 6 && dirInt <= 7)) {
			// left
			resizeLeft = true;
		}

	}

	private void clearInitMoveResize() {
		logger.debug("clear move resize");
		draggingApp = false;
		initMoveResizePacket = null;
		resizeTop = false;
		resizeBottom = false;
		resizeLeft = false;
		resizeRight = false;
	}

	protected boolean isMoveResizing() {
		logger.debug(draggingApp + " " + resizeTop + " " + resizeRight + " " + resizeBottom + " " + resizeLeft);
		return resizeBottom || resizeLeft || resizeRight || resizeTop || draggingApp;
	}
}
