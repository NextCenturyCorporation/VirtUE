/*
 * Copyright (C) 2019 Next Century Corporation
 * 
 * This file may be redistributed and/or modified under either the GPL
 * 2.0 or 3-Clause BSD license. In addition, the U.S. Government is
 * granted government purpose rights. For details, see the COPYRIGHT.TXT
 * file at the root of this project.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 * 
 * SPDX-License-Identifier: (GPL-2.0-only OR BSD-3-Clause)
 */
package com.ncc.savior.desktop.xpra.application.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.Window.Type;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.Closeable;
import java.io.IOException;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

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

/**
 * Controls a SwingFX Application. An Application is defined as window that has
 * its own taskbar item. For example, each firefox window is considered an
 * application.
 *
 *
 */
public class SwingApplication extends XpraApplication implements Closeable {
	private static final Logger logger = LoggerFactory.getLogger(SwingApplication.class);

	// private boolean show;
	private SwingXpraPacketHandler applicationPacketHandler;
	private boolean draggingApp = false;

	private boolean resizeTop;
	private boolean resizeRight;
	private boolean resizeBottom;
	private boolean resizeLeft;
	private int clickSceneX;
	private int clickSceneY;

	private boolean decorated;
	private boolean taskbar;

	private SwingApplication parent;

	protected int titleBarHeight;
	protected int insetWidth;

	private Color color;

	private WindowFrame frame;

	private MouseAdapter mouseAdapter;

	private Rectangle fullScreenBounds;

	private boolean fullscreen;

	public SwingApplication(XpraClient client, NewWindowPacket packet, SwingApplication parent, Color color) {
		super(client, packet.getWindowId());
		this.color = color;
		this.parent = parent;
		init(packet);
	}

	void init(NewWindowPacket packet) {
		logger.debug(packet.toString());
		decorated = isDecorated(packet.getMetadata()) && !(packet instanceof NewWindowOverrideRedirectPacket);
		// Group root = new Group();
		// anchor = new AnchorPane();
		// root.getChildren().add(anchor);
		// scene = new Scene(root, packet.getWidth(), packet.getHeight());
		taskbar = isTaskbar(packet.getMetadata()) && !(packet instanceof NewWindowOverrideRedirectPacket);
		Window parentWindow = parent == null ? null : parent.getWindow();
		frame = WindowFrame.createWindow(packet, parentWindow);
		frame.getContentPane().setSize(packet.getWidth(), packet.getHeight());
		frame.getContentPane().setPreferredSize(new Dimension(packet.getWidth(), packet.getHeight()));
		SwingXpraPacketHandler applicationPacketHandler = new SwingXpraPacketHandler(frame);
		client.addPacketListener(applicationPacketHandler);
		windowManager = new SwingXpraWindowManager(client, packet.getWindowId());
		((SwingXpraWindowManager) windowManager).setColor(color);
		windowManager.setDebugOutput(debugOutput);
		fullScreenBounds = frame.getMaximizedBounds();

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				WindowMetadata meta = packet.getMetadata();
				// boolean isModal = meta.getModal();
				// StageStyle style = (isModal ? StageStyle.UTILITY : StageStyle.DECORATED);
				// if (packet instanceof NewWindowOverrideRedirectPacket) {
				// style = StageStyle.TRANSPARENT;
				// }
				decorated = isDecorated(meta) && !(packet instanceof NewWindowOverrideRedirectPacket);

				try {
					frame.setType(taskbar ? Type.NORMAL : Type.UTILITY);
					frame.setUndecorated(!decorated);
					// TODO
					if (packet instanceof NewWindowOverrideRedirectPacket) {
						frame.setState(JFrame.NORMAL);
						// TODO stage.initModality(Modality.WINDOW_MODAL);
					}
				} catch (Exception e) {
					logger.error("error", e);
				}
				// TODO
				// if (isModal && parent != null) {
				// stage.initModality(Modality.WINDOW_MODAL);
				// stage.initOwner(parent.getStage());
				// stage.setIconified(false);
				// }
				if (meta.getFullscreen()) {
					fullscreen = true;
					fullscreenWindow();
				} else {
					fullscreen = false;
				}

				// stage.setScene(scene);
				// logger.warn("Need to implement container");
				Container container = frame.getContentPane();
				// frame.setContentPane(container);
				((SwingXpraWindowManager) windowManager).setFrame(frame);
				((SwingXpraWindowManager) windowManager).setContainer(container);
				// stage.setX(packet.getX());
				// stage.setY(packet.getY());

				frame.pack();
				try {
					if (!decorated) {
						frame.getContentPane().setBackground(new Color(0, 0, 0, 0));
						frame.setBackground(new Color(0, 0, 0, 0));
					}
				} catch (Throwable t) {
					logger.error("", t);
				}
				insetWidth = frame.getWindow().getInsets().left;
				titleBarHeight = frame.getWindow().getInsets().top;
				// frame.setSize(insetWidth * 2 + packet.getWidth(), insetWidth + titleBarHeight
				// + packet.getHeight());
				int x = packet.getX();
				int y = packet.getY();
				if (x < insetWidth) {
					x = insetWidth;
				}
				if (y < titleBarHeight) {
					y = titleBarHeight;
				}
				frame.getContentPane().setSize(packet.getWidth(), packet.getHeight());
				frame.getContentPane().setPreferredSize(new Dimension(packet.getWidth(), packet.getHeight()));

				if (meta.getFullscreen()) {
					x = 0;
					y = 0;
					insetWidth = 0;
					titleBarHeight = 0;
				}
				frame.setLocation(x - insetWidth, y - titleBarHeight);
				// ((SwingXpraWindowManager) windowManager).setInsetWith(insetWidth);
				// ((SwingXpraWindowManager) windowManager).setTitleBarHeight(titleBarHeight);
				// stage.setWidth(packet.getWidth());
				// stage.setHeight(packet.getHeight());
				frame.setSize(packet.getWidth() + insetWidth * 2, packet.getHeight() + insetWidth + titleBarHeight);
				frame.pack();
				// logger.debug("Frame: " + frame.getSize() + " container=" +
				// frame.getContentPane().getSize() + " packet="
				// + packet.getWidth());
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
					frame.setVisible(true);
				} catch (IOException e) {
					logger.error("Error sending packet=" + packet);
				}
			}
		});
	}

	private void fullscreenWindow() {
		frame.setMaximizedBounds(fullScreenBounds);
		frame.setExtendedState(frame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
		frame.setUndecorated(true);
		frame.requestFocus();
	}

	protected boolean isTaskbar(WindowMetadata meta) {
		boolean tb = !meta.getSkipTaskbar();
		if (meta.getWindowType().contains("MENU")) {
			tb = false;
		}
		return tb;
	}

	protected void initEventHandlers() {
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowIconified(WindowEvent e) {
				onMinimized();
			}

			@Override
			public void windowDeiconified(WindowEvent e) {
				onRestored(getScreenX(), getScreenY(), frame.getWidth(), frame.getHeight());
			}

			// public void windowOpened(WindowEvent e)
			// public void windowDeactivated(WindowEvent e)
			// public void windowClosed(WindowEvent e)
			// public void windowActivated(WindowEvent e)

			@Override
			public void windowClosing(WindowEvent e) {
				try {
					SwingApplication.this.close();
				} catch (IOException ex) {
					logger.error("Error attempting to close application." + SwingApplication.this, ex);
				}
				XpraWindowManager manager = SwingApplication.super.windowManager;
				manager.CloseAllWindows();
			}
		});
		frame.addComponentListener(new ComponentAdapter() {

			// public void componentShown(ComponentEvent e)
			// public void componentHidden(ComponentEvent e)

			@Override
			public void componentResized(ComponentEvent e) {
				Component c = e.getComponent();
				if (c instanceof JFrame) {
					c = ((JFrame) c).getContentPane();
				} else if (c instanceof JDialog) {
					c = ((JDialog) c).getContentPane();
				}
				onSceneSizeChange(c.getWidth(), c.getHeight());
			}

			@Override
			public void componentMoved(ComponentEvent e) {
				Component c = e.getComponent();
				if (c instanceof JFrame) {
					c = ((JFrame) c).getContentPane();
				} else if (c instanceof JDialog) {
					c = ((JDialog) c).getContentPane();
				}
				Point l = c.getLocationOnScreen();
				onLocationChange((int) l.getX(), (int) l.getY(), c.getWidth(), c.getHeight());
			}
		});
		// scene.widthProperty().addListener(new ChangeListener<Number>() {
		// @Override
		// public void changed(ObservableValue<? extends Number> observable, Number
		// oldV, Number newV) {
		// onSceneSizeChange(newV.intValue(), (int) scene.getHeight());
		// }
		// });
		// scene.heightProperty().addListener(new ChangeListener<Number>() {
		// @Override
		// public void changed(ObservableValue<? extends Number> observable, Number
		// oldV, Number newV) {
		// onSceneSizeChange((int) scene.getWidth(), newV.intValue());
		// }
		// });
		// stage.xProperty().addListener(new ChangeListener<Number>() {
		// @Override
		// public void changed(ObservableValue<? extends Number> observable, Number
		// oldValue, Number newValue) {
		// if (!stage.isIconified()) {
		// onLocationChange(getScreenX(), getScreenY(), (int) scene.getWidth(), (int)
		// scene.getHeight());
		// }
		// }
		// });
		// stage.yProperty().addListener(new ChangeListener<Number>() {
		// @Override
		// public void changed(ObservableValue<? extends Number> observable, Number
		// oldValue, Number newValue) {
		// if (!stage.isIconified()) {
		// onLocationChange(getScreenX(), getScreenY(), (int) scene.getWidth(), (int)
		// scene.getHeight());
		// }
		// }
		// });
		mouseAdapter = new MouseAdapter() {

			@Override
			public void mouseReleased(MouseEvent e) {
				if (isMoveResizing()) {
					sendPacket(
							new MapWindowPacket(baseWindowId, getScreenX(), getScreenY(),
									(int) frame.getContentPane().getWidth(), (int) frame.getContentPane().getHeight()),
							"Configure Window");

					clearInitMoveResize();
				}
			}

			@Override
			public void mousePressed(MouseEvent e) {
				clickSceneX = e.getX();
				clickSceneY = e.getY();
			}

			@Override
			public void mouseDragged(MouseEvent event) {
				if (draggingApp) {
					frame.setLocation((event.getXOnScreen() - clickSceneX), (event.getYOnScreen() - clickSceneY));
				}
				int nW = frame.getWidth();
				int nH = frame.getHeight();
				int nX = frame.getWindow().getX();
				int nY = frame.getWindow().getY();
				if (resizeTop) {
					int ydelta = frame.getWindow().getY() - event.getYOnScreen();
					nH = frame.getHeight() + ydelta;
					nY = event.getYOnScreen();
				}
				if (resizeLeft) {
					int xdelta = frame.getWindow().getX() - event.getXOnScreen();
					nW = frame.getWidth() + xdelta;
					nX = event.getXOnScreen();
				}
				if (resizeRight) {
					nW = event.getXOnScreen() - frame.getWindow().getX();
				}
				if (resizeBottom) {
					nH = event.getYOnScreen() - frame.getWindow().getY();
				}
				frame.setSize(nW, nH);
				frame.setLocation(nX, nY);
			}

		};

		((SwingXpraWindowManager) windowManager).setMouseAdapter(mouseAdapter);
		// scene.setOnMousePressed(new EventHandler<MouseEvent>() {
		// @Override
		// public void handle(MouseEvent event) {
		// clickSceneX = event.getSceneX();
		// clickSceneY = event.getSceneY();
		// }
		// });
		// scene.setOnMouseReleased(new EventHandler<MouseEvent>() {
		// @Override
		// public void handle(MouseEvent event) {
		// if (isMoveResizing()) {
		// sendPacket(new MapWindowPacket(baseWindowId, getScreenX(), getScreenY(),
		// (int) scene.getWidth(),
		// (int) scene.getHeight()), "Configure Window");
		//
		// clearInitMoveResize();
		// }
		// }
		// });

		// scene.setOnMouseDragged(new EventHandler<MouseEvent>() {
		// @Override
		// public void handle(MouseEvent event) {
		// if (draggingApp) {
		// stage.setX(event.getScreenX() - clickSceneX);
		// stage.setY(event.getScreenY() - clickSceneY);
		// }
		// if (resizeTop) {
		// double ydelta = stage.getY() - event.getScreenY();
		// stage.setHeight(stage.getHeight() + ydelta);
		// stage.setY(event.getScreenY());
		// }
		// if (resizeLeft) {
		// double xdelta = stage.getX() - event.getScreenX();
		// stage.setWidth(stage.getWidth() + xdelta);
		// stage.setX(event.getScreenX());
		// }
		// if (resizeRight) {
		// double width = event.getScreenX() - stage.getX();
		// stage.setWidth(width);
		// }
		// if (resizeBottom) {
		// double height = event.getScreenY() - stage.getY();
		// stage.setHeight(height);
		// }
		// }
		// });
	}

	/**
	 * Scene's Y coordinate in Screen coordinates
	 *
	 * @return
	 */
	protected int getScreenY() {
		return (frame.getWindow().getY() + titleBarHeight);
	}

	/**
	 * Scene's X coordinate in Screen coordinates
	 *
	 * @return
	 */
	protected int getScreenX() {
		return (frame.getWindow().getX() + insetWidth);
	}

	protected void onSceneSizeChange(int width, int height) {
		windowManager.resizeWindow(baseWindowId, width, height);
		onLocationChange(getScreenX(), getScreenY(), width, height);
	}

	@Override
	public void Show() {
		if (frame != null) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					frame.setVisible(true);
				}
			});
		}
	}

	@Override
	public void doClose() throws IOException {
		if (applicationPacketHandler != null) {
			client.removePacketListener(applicationPacketHandler);
		}
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				frame.setVisible(false);
				frame.dispose();
			}
		});
	}

	public Window getWindow() {
		return frame.getWindow();
	}

	@Override
	public void maximize() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				frame.setMaximizedBounds(GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds());
				frame.setExtendedState(frame.getExtendedState() | Frame.MAXIMIZED_BOTH);
				// stage.setIconified(false);
				// stage.setMaximized(true);
				// stage.setMaxHeight(Screen.getPrimary().getVisualBounds().getHeight());
				// stage.setMinHeight(Screen.getPrimary().getVisualBounds().getHeight());
				// stage.setHeight(Screen.getPrimary().getVisualBounds().getHeight());
			}
		});
	}

	@Override
	public void minimize() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				frame.setState(JFrame.ICONIFIED);
			}
		});
	}

	@Override
	public void restore() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				// stage.setIconified(false);
				// if (stage.isMaximized()) {
				// stage.setMaxHeight(Screen.getPrimary().getVisualBounds().getHeight());
				// stage.setMinHeight(Screen.getPrimary().getVisualBounds().getHeight());
				// stage.setHeight(Screen.getPrimary().getVisualBounds().getHeight());
				// }
				frame.setState(JFrame.NORMAL);
			}
		});
	}

	@Override
	public void unMaximize() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				frame.setExtendedState(~JFrame.MAXIMIZED_BOTH & frame.getExtendedState());
				// stage.setIconified(false);
				// stage.setMaximized(false);
				// stage.setMinHeight(10);
			}
		});
	}

	@Override
	public void initiateMoveResize(InitiateMoveResizePacket packet) {
		clearInitMoveResize();
		if ((frame.getExtendedState() & JFrame.MAXIMIZED_BOTH) == JFrame.MAXIMIZED_BOTH) {
			return;
		}
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

	private boolean isDecorated(WindowMetadata meta) {
		boolean defaultDecorations = true;
		for (String type : meta.getWindowType()) {
			if (noToolbarTypes.contains(type)) {
				defaultDecorations = false;
			}
		}
		boolean decorated = meta.getDecorations(defaultDecorations) ? true : false;
		return decorated;
	}

	@Override
	public void setLocationSize(int x, int y, int width, int height) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				// stage.setX(x - insetWidth);
				// stage.setY(y - titleBarHeight);
				frame.setLocation((x - insetWidth), (y - titleBarHeight));
				// stage.setWidth(width + 2 * insetWidth);
				// stage.setHeight(height + insetWidth + titleBarHeight);
				frame.setSize((width + 2 * insetWidth), (height + insetWidth + titleBarHeight));
			}
		});
	}

	public WindowFrame getWindowFrame() {
		return frame;
	}

	@Override
	public void fullscreen() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					if (!fullscreen) {
						fullscreen = true;

						frame.switchToFullscreen();
						titleBarHeight = 0;
						insetWidth = 0;
						onSceneSizeChange(frame.getWindow().getWidth(), frame.getWindow().getHeight());

						// frame.setMaximizedBounds(fullScreenBounds);
						// frame.setExtendedState(frame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
						// frame.requestFocus();

					}
				} catch (Throwable t) {
					logger.error("err", t);
				}
			}
		});
	}

	@Override
	public void notFullScreen() {

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {

				try {
					if (fullscreen) {
						fullscreen = false;
						frame.switchFromFullscreen();
						insetWidth = frame.getWindow().getInsets().left;
						titleBarHeight = frame.getWindow().getInsets().top;
						onSceneSizeChange(frame.getWindow().getWidth() - insetWidth * 2,
								frame.getWindow().getHeight() - insetWidth - titleBarHeight);
						// frame.setExtendedState(frame.getExtendedState() & ~JFrame.MAXIMIZED_BOTH);
						// if (decorated) {
						// frame.getRootPane().setWindowDecorationStyle(JRootPane.FRAME);
						// }
					}
				} catch (Throwable t) {
					logger.error("err", t);
				}
			}
		});
	}
}
