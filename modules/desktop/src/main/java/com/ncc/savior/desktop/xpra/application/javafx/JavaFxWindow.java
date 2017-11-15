package com.ncc.savior.desktop.xpra.application.javafx;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.xpra.application.IFocusNotifier;
import com.ncc.savior.desktop.xpra.application.XpraWindow;
import com.ncc.savior.desktop.xpra.protocol.IPacketSender;
import com.ncc.savior.desktop.xpra.protocol.keyboard.IKeyboard;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.DrawPacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.InitiateMoveResizePacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.InitiateMoveResizePacket.MoveResizeDirection;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.NewWindowPacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.WindowIconPacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.WindowMetadata;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.WindowMetadataPacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.WindowMoveResizePacket;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * Controls a single window for a {@link JavaFxApplication}. A window is defined
 * by the Xpra protocol and is considered any panel or screen that appears
 * overtop another view. For example, tooltips, history, or settings panels are
 * often their own window inside a single application. Applications typically
 * contain a root window which displays the 'normal' view. Most windows (Besides
 * the root window) are short lived and will be created and then destroyed in
 * fairly short order.
 *
 *
 */
public class JavaFxWindow extends XpraWindow {
	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(JavaFxWindow.class);

	private Canvas canvas;
	private Stage stage;

	private List<String> type;

	private String title;

	private boolean closed;

	private boolean draggingApp = false;

	private InitiateMoveResizePacket initMoveResizePacket;

	private boolean resizeTop;

	private boolean resizeRight;

	private boolean resizeBottom;

	private boolean resizeLeft;

	public JavaFxWindow(NewWindowPacket packet, IPacketSender packetSender, IKeyboard keyboard,
			IFocusNotifier focusNotifier) {
		super(packet, packetSender, keyboard, focusNotifier);
		// logger.debug("ID: " + packet.getWindowId() + " Parent: " +
		// packet.getMetadata().getParentId() + " "
		// + packet.getType().toString() + " - "
		// + packet.toString());

		WindowMetadata metadata = packet.getMetadata();
		title = metadata.getTitle();
		type = metadata.getWindowType();
	}

	public void initJavaFx(Canvas canvas, Stage stage) {
		this.canvas = canvas;
		this.stage = stage;
		if (type.contains("NORMAL")) {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					stage.setTitle(title);
				}
			});
		}

		this.canvas.setOnMouseMoved(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				List<String> modifiers = JavaFxUtils.getModifiers(event);
				onMouseMove((int) event.getSceneX(), (int) event.getSceneY(), modifiers);
			}
		});
		this.canvas.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				List<String> modifiers = JavaFxUtils.getModifiers(event);
				onWindowFocus();
				onMousePress(event.getButton().ordinal(), (int) event.getSceneX(), (int) event.getSceneY(), modifiers);
			}
		});
		this.canvas.setOnMouseReleased(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				if (isMoveResizing()) {
					// sendPacket(new ConfigureWindowPacket(id, (int) stage.getX(), (int)
					// stage.getY(),
					// (int) stage.getWidth(), (int) stage.getHeight()), "Configure Window");

					clearInitMoveResize();
				}
				List<String> modifiers = JavaFxUtils.getModifiers(event);
				onMouseRelease(event.getButton().ordinal(), (int) event.getSceneX(), (int) event.getSceneY(),
						modifiers);
			}
		});
		this.canvas.setOnMouseDragged(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				boolean resizing = false;
				if (draggingApp) {
					int sceneXStart = initMoveResizePacket.getxRoot();
					int sceneYStart = initMoveResizePacket.getyRoot();
					stage.setX(event.getScreenX() - sceneXStart);
					stage.setY(event.getScreenY() - sceneYStart);
					resizing = true;
				}
				if (resizeTop) {
					double ydelta = stage.getY() - event.getScreenY();
					stage.setHeight(stage.getHeight() + ydelta);
					stage.setY(event.getScreenY());
					resizing = true;
				}
				if (resizeLeft) {
					double xdelta = stage.getX() - event.getScreenX();
					stage.setWidth(stage.getWidth() + xdelta);
					stage.setX(event.getScreenX());
					resizing = true;
				}
				if (resizeRight) {
					double width = event.getScreenX() - stage.getX();
					stage.setWidth(width);
					resizing = true;
				}
				if (resizeBottom) {
					double height = event.getScreenY() - stage.getY();
					stage.setHeight(height);
					resizing = true;
				}
				if (!resizing) {
					List<String> modifiers = JavaFxUtils.getModifiers(event);
					onMouseMove((int) event.getSceneX(), (int) event.getSceneY(), modifiers);
				}
			}
		});
		this.canvas.setOnScroll(new EventHandler<ScrollEvent>() {
			@Override
			public void handle(ScrollEvent event) {
				onMouseScroll(0, (int) event.getDeltaY(), (int) event.getSceneX(), (int) event.getSceneY());
			}
		});
		graphicsSet = true;
	}

	protected boolean isMoveResizing() {
		logger.debug(draggingApp + " " + resizeTop + " " + resizeRight + " " + resizeBottom + " " + resizeLeft);
		return resizeBottom || resizeLeft || resizeRight || resizeTop || draggingApp;
	}

	@Override
	public void doClose() {
		closed = true;
	}

	@Override
	public void draw(DrawPacket packet) {
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				try {
					if (closed) {
						return;
					}
					Image img = ImageEncoder.decodeImage(packet.getEncoding(), packet.getData());
					if (img != null && !closed && canvas != null) {
						GraphicsContext g = canvas.getGraphicsContext2D();
						// g.setGlobalBlendMode(BlendMode.SCREEN);
						// g.setGlobal
						// logger.debug("ID:" + packet.getWindowId() + " Window:" + this.toString());
						g.drawImage(img, packet.getX(), packet.getY(), packet.getWidth(), packet.getHeight());

						if (debugOutput) {
							g.setStroke(Color.BLUE);
							g.setFill(Color.GREEN);
							g.setLineWidth(2);
							g.strokeRect(1, 1, canvas.getWidth() - 2, canvas.getHeight() - 2);
							g.fillText("ID: " + id, 2, 10);
						}

						sendDamageSequence(packet);
					}
				} catch (Exception e) {
					throw new RuntimeException("Failed decoding image: " + packet.getEncoding(), e);
				}
			}
		});

	}

	@Override
	public void onWindowMoveResize(WindowMoveResizePacket packet) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				AnchorPane.setLeftAnchor(canvas, (double) packet.getX());
				AnchorPane.setTopAnchor(canvas, (double) packet.getY());
				canvas.setWidth(packet.getWidth());
				canvas.setHeight(packet.getHeight());
			}
		});
	}

	public Canvas getCanvas() {
		return canvas;
	}

	@Override
	public void setWindowIcon(WindowIconPacket packet) {
		Image icon = ImageEncoder.decodeImage(packet.getEncoding(), packet.getData());
		if (icon != null) {
			Platform.runLater(new Runnable() {

				@Override
				public void run() {
					stage.getIcons().clear();
					stage.getIcons().add(icon);
				}
			});
		}
	}

	@Override
	public void updateWindowMetadata(WindowMetadataPacket packet) {
		String title = packet.getMetadata().getTitle();
		if (type.contains("NORMAL") && title != null) {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					stage.setTitle(title);
				}
			});
		}
	}

	@Override
	public String toString() {
		return "JavaFxWindow [canvas=" + canvas + ", stage=" + stage + ", type=" + type + ", title=" + title
				+ ", closed=" + closed + ", id=" + id + ", graphicsSet=" + graphicsSet + ", keyboard=" + keyboard + "]";
	}

	@Override
	public void resize(int width, int height) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				canvas.setWidth(width);
				canvas.setHeight(height);
			}
		});
	}

	@Override
	public void initiateMoveResize(InitiateMoveResizePacket packet) {
		clearInitMoveResize();
		MoveResizeDirection dir = packet.getDirection();
		logger.debug(packet.toString());
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
}
