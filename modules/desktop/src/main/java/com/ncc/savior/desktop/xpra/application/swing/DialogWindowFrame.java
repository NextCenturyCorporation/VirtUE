package com.ncc.savior.desktop.xpra.application.swing;

import java.awt.Container;
import java.awt.Image;
import java.awt.Rectangle;

import javax.swing.JDialog;
import javax.swing.JFrame;

public class DialogWindowFrame extends WindowFrame {

	private JDialog frame;

	public DialogWindowFrame(JDialog jDialog) {
		super(jDialog);
		this.frame = jDialog;
	}

	@Override
	public Container getContentPane() {
		return frame.getContentPane();
	}

	@Override
	public Rectangle getMaximizedBounds() {
		return null;
	}

	@Override
	public void setUndecorated(boolean b) {
		frame.setUndecorated(b);
	}

	@Override
	public void setState(int normal) {

	}

	@Override
	public void setMaximizedBounds(Rectangle fullScreenBounds) {

	}

	@Override
	public int getExtendedState() {
		return JFrame.NORMAL;
	}

	@Override
	public void setExtendedState(int i) {

	}

	@Override
	public void setTitle(String title) {
		frame.setTitle(title);
	}

	@Override
	public void setIconImage(Image icon) {
		frame.setIconImage(icon);
	}

}
