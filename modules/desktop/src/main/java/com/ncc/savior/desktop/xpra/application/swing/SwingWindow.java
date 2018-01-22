package com.ncc.savior.desktop.xpra.application.swing;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.List;

import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.xpra.application.IFocusNotifier;
import com.ncc.savior.desktop.xpra.application.XpraApplication;
import com.ncc.savior.desktop.xpra.application.XpraWindow;
import com.ncc.savior.desktop.xpra.protocol.IPacketSender;
import com.ncc.savior.desktop.xpra.protocol.keyboard.IKeyboard;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.DrawPacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.NewWindowPacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.WindowIconPacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.WindowMetadata;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.WindowMetadataPacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.WindowMoveResizePacket;

/**
 * Controls a single window for a {@link SwingApplication}. A window is defined
 * by the Xpra protocol and is considered any panel or screen that appears
 * overtop another view. For example, tooltips, history, or settings panels are
 * often their own window inside a single application. Applications typically
 * contain a root window which displays the 'normal' view. Most windows (Besides
 * the root window) are short lived and will be created and then destroyed in
 * fairly short order.
 *
 *
 */
public class SwingWindow extends XpraWindow {
	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(SwingWindow.class);

	private JCanvas canvas;

	private List<String> type;

	private String title;

	private boolean closed;

	private Color color;

	private WindowFrame window;

	public SwingWindow(NewWindowPacket packet, IPacketSender packetSender, IKeyboard keyboard,
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

	public void initSwing(JCanvas canvas, WindowFrame frame) {
		this.canvas = canvas;
		this.window = frame;
		if (type.contains("NORMAL")) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					window.setTitle(title);
				}
			});
		}

		this.canvas.addMouseListener(new MouseListener() {

			@Override
			public void mouseReleased(MouseEvent event) {
				List<String> modifiers = SwingUtils.getModifiers(event);
				onWindowFocus();
				onMouseRelease(event.getButton(), (int) event.getXOnScreen(), (int) event.getYOnScreen(), modifiers);
			}

			@Override
			public void mousePressed(MouseEvent event) {
				List<String> modifiers = SwingUtils.getModifiers(event);
				onWindowFocus();
				onMousePress(event.getButton(), (int) event.getXOnScreen(), (int) event.getYOnScreen(), modifiers);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseClicked(MouseEvent e) {
				// TODO Auto-generated method stub

			}
		});
		this.canvas.addMouseMotionListener(new MouseMotionListener() {

			@Override
			public void mouseMoved(MouseEvent e) {
				List<String> modifiers = SwingUtils.getModifiers(e);
				onMouseMove((int) e.getXOnScreen(), (int) e.getYOnScreen(), modifiers);

			}

			@Override
			public void mouseDragged(MouseEvent event) {
				List<String> modifiers = SwingUtils.getModifiers(event);
				onMouseMove((int) event.getXOnScreen(), (int) event.getYOnScreen(), modifiers);
			}
		});
		this.canvas.addMouseWheelListener(new MouseWheelListener() {

			@Override
			public void mouseWheelMoved(MouseWheelEvent event) {
				// XonScreen seems to always give 0.
				Point locOS = canvas.getLocationOnScreen();
				int x = (int) (event.getX() + locOS.getX());
				int y = (int) (event.getY() + locOS.getY());
				onMouseScroll(0, (int) (event.getPreciseWheelRotation() * -20), x, y);

			}
		});
		// logger.warn("Mouse move on canvas not implemented");
		// this.canvas.setOnMouseMoved(new EventHandler<MouseEvent>() {
		// @Override
		// public void handle(MouseEvent event) {
		// List<String> modifiers = JavaFxUtils.getModifiers(event);
		// onMouseMove((int) event.getScreenX(), (int) event.getScreenY(), modifiers);
		// }
		// });
		// this.canvas.setOnMousePressed(new EventHandler<MouseEvent>() {
		// @Override
		// public void handle(MouseEvent event) {
		// List<String> modifiers = JavaFxUtils.getModifiers(event);
		// onWindowFocus();
		// onMousePress(event.getButton().ordinal(), (int) event.getScreenX(), (int)
		// event.getScreenY(),
		// modifiers);
		// }
		// });
		// this.canvas.setOnMouseReleased(new EventHandler<MouseEvent>() {
		// @Override
		// public void handle(MouseEvent event) {
		// List<String> modifiers = JavaFxUtils.getModifiers(event);
		// onMouseRelease(event.getButton().ordinal(), (int) event.getScreenX(), (int)
		// event.getScreenY(),
		// modifiers);
		// }
		// });
		// this.canvas.setOnMouseDragged(new EventHandler<MouseEvent>() {
		// @Override
		// public void handle(MouseEvent event) {
		// List<String> modifiers = JavaFxUtils.getModifiers(event);
		// onMouseMove((int) event.getScreenX(), (int) event.getScreenY(), modifiers);
		// }
		// });
		// this.canvas.setOnScroll(new EventHandler<ScrollEvent>() {
		// @Override
		// public void handle(ScrollEvent event) {
		// onMouseScroll(0, (int) event.getDeltaY(), (int) event.getScreenX(), (int)
		// event.getScreenY());
		// }
		// });
		graphicsSet = true;
	}

	@Override
	public void doClose() {
		closed = true;
		if (window != null) {
			window.setVisible(false);
			window.getWindow().dispose();
		}
	}

	@Override
	public void draw(DrawPacket packet) {
		// logger.debug("Draw: " + packet);
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				try {
					if (closed) {
						return;
					}
					Image img = SwingImageEncoder.decodeImage(packet.getEncoding(), packet.getData());
					if (img != null && !closed && canvas != null) {
						Graphics g = canvas.getGraphics();
						// g.setGlobalBlendMode(BlendMode.SCREEN);
						// g.setGlobal
						// logger.debug("ID:" + packet.getWindowId() + " Window:" + this.toString());
						g.drawImage(img, packet.getX(), packet.getY(), packet.getWidth(), packet.getHeight(), null);
						if (color != null && isMainWindow()) {
							g.setColor(color);
							// g.setFill(Color.GREEN);
							g.drawLine(0, 0, canvas.getWidth(), canvas.getHeight());
						}
						if (debugOutput) {
							logger.warn("Debug output not implemented");
							// g.setStroke(Color.BLUE);
							// g.setFill(Color.GREEN);
							// g.setLineWidth(2);
							// g.strokeRect(1, 1, canvas.getWidth() - 2, canvas.getHeight() - 2);
							// g.fillText("ID: " + id, 2, 10);
						}
						g.dispose();
						canvas.repaint();
						sendDamageSequence(packet);
					}
				} catch (Exception e) {
					throw new RuntimeException("Failed decoding image: " + packet.getEncoding(), e);
				}
			}
		});

	}

	protected boolean isMainWindow() {
		for (String type : this.type) {
			if (XpraApplication.noToolbarTypes.contains(type)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void onWindowMoveResize(WindowMoveResizePacket packet) {
		// SwingUtilities.invokeLater(new Runnable() {
		// @Override
		// public void run() {
		// AnchorPane.setLeftAnchor(canvas, (double) packet.getX());
		// AnchorPane.setTopAnchor(canvas, (double) packet.getY());
		// canvas.setWidth(packet.getWidth());
		// canvas.setHeight(packet.getHeight());
		// }
		// });
	}

	public JCanvas getCanvas() {
		return canvas;
	}

	@Override
	public void setWindowIcon(WindowIconPacket packet) {
		Image icon = SwingImageEncoder.decodeImage(packet.getEncoding(), packet.getData());
		if (icon != null) {
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					window.setIconImage(icon);
					// stage.getIcons().clear();
					// stage.getIcons().add(icon);
				}
			});
		}
	}

	@Override
	public void updateWindowMetadata(WindowMetadataPacket packet) {
		String title = packet.getMetadata().getTitle();
		if (type.contains("NORMAL") && title != null) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					window.setTitle(packet.getMetadata().getTitle());
				}
			});
		}
	}

	@Override
	public void resize(int width, int height) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				canvas.getParent().setSize(width, height);
				canvas.setsize(width, height);
			}
		});
	}

	public void setColor(Color color) {
		this.color = color;
	}
}
