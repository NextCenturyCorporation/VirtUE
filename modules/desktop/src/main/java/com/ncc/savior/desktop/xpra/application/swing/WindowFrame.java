package com.ncc.savior.desktop.xpra.application.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog.ModalityType;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.Window.Type;
import java.awt.event.ComponentAdapter;
import java.awt.event.KeyAdapter;
import java.awt.event.WindowAdapter;

import javax.swing.JDialog;
import javax.swing.JFrame;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.xpra.protocol.packet.dto.NewWindowPacket;

/**
 * An abstraction layer usually around {@link JFrame}s and {@link JDialog}s.
 * This also helps us support fullscreening a single window. Most methods are
 * pass-through methods to the Swing implementation.
 *
 *
 */
public abstract class WindowFrame {
	private static final Logger logger = LoggerFactory.getLogger(WindowFrame.class);
	// main currently used window. In most instances, this is the only window. Some
	// windows will need to be switched to fullscreen and those will need to create
	// another JFrame to support that. When that happens, the window will switch
	// between 2 java windows.
	private Window window;

	// used to switch between fullscreen or not. This represents the currently none
	// visible window and will often be null.
	private Window otherWindow;

	public WindowFrame(Window window) {
		this.window = window;
	}

	public abstract Container getContentPane();

	public abstract Rectangle getMaximizedBounds();

	public void setType(Type type) {
		window.setType(type);
	}

	public abstract void setUndecorated(boolean b);

	public abstract void setState(int normal);

	public void setBackground(Color color) {
		window.setBackground(color);
	}

	public void pack() {
		window.pack();
	}

	public void setVisible(boolean b) {
		window.setVisible(b);
	}

	public int getWidth() {
		return window.getWidth();
	}

	public int getHeight() {
		return window.getHeight();
	}

	public void setLocation(int x, int y) {
		window.setLocation(x, y);
	}

	public void setSize(int x, int y) {
		window.setSize(x, y);
	}

	public abstract void setMaximizedBounds(Rectangle fullScreenBounds);

	public abstract int getExtendedState();

	public abstract void setExtendedState(int i);

	public void requestFocus() {
		window.requestFocus();
	}

	public static WindowFrame createWindow(NewWindowPacket packet, Window owner) {
		boolean modal = packet.getMetadata().getModal();
		WindowFrame frame;
		if (owner != null) {
			JDialog dialog = new JDialog(owner);
			dialog.setFocusTraversalKeysEnabled(false);
			if (modal) {
				dialog.setModalityType(ModalityType.DOCUMENT_MODAL);
			} else {
				dialog.setModalityType(ModalityType.MODELESS);
			}
			// dialog.setLocation(packet.getX(), packet.getY());
			frame = new DialogWindowFrame(dialog);
		} else {
			JFrame jframe = new JFrame();
			jframe.setFocusTraversalKeysEnabled(false);
			frame = new NormalWindowFrame(jframe);
		}
		return frame;
	}

	public Window getWindow() {
		return window;
	}

	public abstract void setTitle(String title);

	public abstract void setIconImage(Image icon);

	public void switchToFullscreen() {
		Container previousCP = getContentPane();
		Window tmp = otherWindow;
		otherWindow = window;
		window = tmp;
		window = doSwitchToFullscreen(window, otherWindow);
		Container newCP = getContentPane();
		Component canvas = previousCP.getComponent(0);
		previousCP.remove(0);
		newCP.add(canvas);
		canvas.setSize(newCP.getSize());
		newCP.repaint();
		requestFocus();
		otherWindow.setVisible(false);
	}

	protected abstract Window doSwitchToFullscreen(Window window, Window previousWindow);

	protected abstract Window doSwitchFromFullscreen(Window window, Window previousWindow);

	public void switchFromFullscreen() {
		Container previousCP = getContentPane();
		Window tmp = otherWindow;
		otherWindow = window;
		window = tmp;
		window = doSwitchFromFullscreen(window, otherWindow);
		Container newCP = getContentPane();
		Component canvas = previousCP.getComponent(0);
		previousCP.remove(0);
		newCP.add(canvas);

		requestFocus();
		otherWindow.setVisible(false);
		canvas.setSize(newCP.getSize());
		newCP.repaint();
	}

	public void addComponentListener(ComponentAdapter componentAdapter) {
		window.addComponentListener(componentAdapter);
		if (otherWindow != null) {
			otherWindow.addComponentListener(componentAdapter);
		}
	}

	public void addWindowListener(WindowAdapter windowAdapter) {
		window.addWindowListener(windowAdapter);
		if (otherWindow != null) {
			otherWindow.addWindowListener(windowAdapter);
		}
	}

	public void addKeyListener(KeyAdapter keyAdapter) {
		window.addKeyListener(keyAdapter);
		if (otherWindow != null) {
			otherWindow.addKeyListener(keyAdapter);
		}
	}

	public void dispose() {
		window.dispose();
		if (otherWindow != null) {
			try {
				otherWindow.dispose();
			} catch (RuntimeException e) {
				logger.error("Error disposing other window", e);
			}
		}
	}

}
