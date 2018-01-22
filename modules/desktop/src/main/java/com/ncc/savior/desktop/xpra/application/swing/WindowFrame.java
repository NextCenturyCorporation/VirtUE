package com.ncc.savior.desktop.xpra.application.swing;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dialog.ModalityType;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.Window.Type;

import javax.swing.JDialog;
import javax.swing.JFrame;

import com.ncc.savior.desktop.xpra.protocol.packet.dto.NewWindowPacket;

public abstract class WindowFrame {

	private Window window;

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
		if (owner != null) {
			JDialog dialog = new JDialog(owner);
			if (modal) {
				dialog.setModalityType(ModalityType.DOCUMENT_MODAL);
			} else {
				dialog.setModalityType(ModalityType.MODELESS);
			}
			// dialog.setLocation(packet.getX(), packet.getY());
			return new DialogWindowFrame(dialog);
		} else {
			return new NormalWindowFrame(new JFrame());
		}
	}

	public Window getWindow() {
		return window;
	}

	public abstract void setTitle(String title);

	public abstract void setIconImage(Image icon);

}
