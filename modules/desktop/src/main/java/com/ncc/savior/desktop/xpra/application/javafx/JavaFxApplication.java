package com.ncc.savior.desktop.xpra.application.javafx;

import java.io.Closeable;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.xpra.XpraClient;
import com.ncc.savior.desktop.xpra.application.XpraApplication;
import com.ncc.savior.desktop.xpra.application.XpraWindowManager;
import com.ncc.savior.desktop.xpra.protocol.IPacketSender;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.InitiateMoveResizePacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.InitiateMoveResizePacket.MoveResizeDirection;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.MapWindowPacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.NewWindowOverrideRedirectPacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.NewWindowPacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.WindowMetadata;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
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
	private boolean show;
	private JavaFxXpraPacketHandler applicationPacketHandler;
	private boolean draggingApp = false;

	private boolean resizeTop;
	private boolean resizeRight;
	private boolean resizeBottom;
	private boolean resizeLeft;
	private double clickSceneX;
	private double clickSceneY;

	private boolean decorated;

	private JavaFxApplication parent;

	protected double titleBarHeight;
	protected double insetWidth;

	public JavaFxApplication(XpraClient client, NewWindowPacket packet, JavaFxApplication parent) {
		super(client, packet.getWindowId());
		this.parent = parent;
		init(packet);
	}

	void init(NewWindowPacket packet) {
		decorated = packet.getMetadata().getDecorations();
		Group root = new Group();
		anchor = new AnchorPane();
		root.getChildren().add(anchor);
		scene = new Scene(root, packet.getWidth(), packet.getHeight());
		JavaFxXpraPacketHandler applicationPacketHandler = new JavaFxXpraPacketHandler(scene);
		client.addPacketListener(applicationPacketHandler);
		windowManager = new JavaFxXpraWindowManager(client, packet.getWindowId());
		windowManager.setDebugOutput(debugOutput);

		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				WindowMetadata meta = packet.getMetadata();
				boolean isModal = meta.getModal();
				// StageStyle style = (isModal ? StageStyle.UTILITY : StageStyle.DECORATED);
				// if (packet instanceof NewWindowOverrideRedirectPacket) {
				// style = StageStyle.TRANSPARENT;
				// }
				StageStyle style = getStageStyle(meta);
				stage = new Stage(style);

				if (packet instanceof NewWindowOverrideRedirectPacket) {
					stage.setIconified(false);
					stage.initModality(Modality.WINDOW_MODAL);
				}
				if (isModal && parent != null) {
					stage.initModality(Modality.WINDOW_MODAL);
					stage.initOwner(parent.getStage());
					stage.setIconified(false);
				}

				stage.setScene(scene);
				((JavaFxXpraWindowManager) windowManager).setStage(stage);
				((JavaFxXpraWindowManager) windowManager).setAnchor(anchor);
				stage.setX(packet.getX());
				stage.setY(packet.getY());
				stage.show();
				insetWidth = (stage.getWidth() - scene.getWidth()) / 2;
				titleBarHeight = stage.getHeight() - scene.getHeight() - insetWidth;
				((JavaFxXpraWindowManager) windowManager).setInsetWith((int) insetWidth);
				((JavaFxXpraWindowManager) windowManager).setTitleBarHeight((int) titleBarHeight);
				// stage.setWidth(packet.getWidth());
				// stage.setHeight(packet.getHeight());
				initEventHandlers();
				IPacketSender sender = client.getPacketSender();
				MapWindowPacket sendPacket = new MapWindowPacket(packet.getWindowId(), getScreenX(), getScreenY(),
						packet.getWidth(), packet.getHeight());

				// ConfigureWindowPacket sendPacket = new
				// ConfigureWindowPacket(packet.getWindowId(),
				// (int) stage.getX(), (int) stage.getY(), (int) stage.getWidth(), (int)
				// stage.getHeight());
				try {
					sender.sendPacket(sendPacket);
				} catch (IOException e) {
					logger.error("Error sending packet=" + packet);
				}
			}
		});
	}

	protected void initEventHandlers() {

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
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if (newValue) {
					onMinimized();
				} else {
					onRestored(getScreenX(), getScreenY(), (int) scene.getWidth(), (int) scene.getHeight());
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
		stage.xProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				if (!stage.isIconified()) {
					onLocationChange(getScreenX(), getScreenY(), (int) scene.getWidth(), (int) scene.getHeight());
				}
			}
		});
		stage.yProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				if (!stage.isIconified()) {
					onLocationChange(getScreenX(), getScreenY(), (int) scene.getWidth(), (int) scene.getHeight());
				}
			}
		});
		scene.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				clickSceneX = event.getSceneX();
				clickSceneY = event.getSceneY();
			}
		});
		scene.setOnMouseReleased(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				if (isMoveResizing()) {
					sendPacket(new MapWindowPacket(baseWindowId, getScreenX(), getScreenY(), (int) scene.getWidth(),
							(int) scene.getHeight()), "Configure Window");

					clearInitMoveResize();
				}
			}
		});

		scene.setOnMouseDragged(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				if (draggingApp) {
					stage.setX(event.getScreenX() - clickSceneX);
					stage.setY(event.getScreenY() - clickSceneY);
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
	}

	/**
	 * Scene's Y coordinate in Screen coordinates
	 *
	 * @return
	 */
	protected int getScreenY() {
		return (int) (stage.getY() + titleBarHeight);
	}

	/**
	 * Scene's X coordinate in Screen coordinates
	 *
	 * @return
	 */
	protected int getScreenX() {
		return (int) (stage.getX() + insetWidth);
	}

	protected void onSceneSizeChange(int width, int height) {
		windowManager.resizeWindow(baseWindowId, width, height);
		onLocationChange(getScreenX(), getScreenY(), width, height);
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
		return "JavaFxApplication [anchor=" + anchor + ", stage=" + stage + ", scene=" + scene + ", show=" + show
				+ ", applicationPacketHandler=" + applicationPacketHandler + ", draggingApp=" + draggingApp
				+ ", resizeTop=" + resizeTop + ", resizeRight=" + resizeRight + ", resizeBottom=" + resizeBottom
				+ ", resizeLeft=" + resizeLeft + ", clickSceneX=" + clickSceneX + ", clickSceneY=" + clickSceneY
				+ ", decorated=" + decorated + ", parent=" + parent + ", baseWindowId=" + baseWindowId + "]";
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
			// initMoveResizePacket = packet;
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
		draggingApp = false;
		// initMoveResizePacket = null;
		resizeTop = false;
		resizeBottom = false;
		resizeLeft = false;
		resizeRight = false;
		// clickSceneX = 0;
		// clickSceneY = 0;
	}

	protected boolean isMoveResizing() {
		return resizeBottom || resizeLeft || resizeRight || resizeTop || draggingApp;
	}

	private StageStyle getStageStyle(WindowMetadata meta) {
		boolean defaultDecorations = true;
		for (String type : meta.getWindowType()) {
			if (noToolbarTypes.contains(type)) {
				defaultDecorations = false;
			}
		}
		StageStyle style = meta.getDecorations(defaultDecorations) ? StageStyle.DECORATED : StageStyle.TRANSPARENT;
		return style;
	}

	@Override
	public void setLocationSize(int x, int y, int width, int height) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				stage.setX(x - insetWidth);
				stage.setY(y - titleBarHeight);
				stage.setWidth(width + 2 * insetWidth);
				stage.setHeight(height + insetWidth + titleBarHeight);
			}
		});
	}
}
