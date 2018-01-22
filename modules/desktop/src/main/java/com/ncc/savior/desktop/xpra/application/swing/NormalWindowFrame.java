package com.ncc.savior.desktop.xpra.application.swing;

import java.awt.Container;
import java.awt.Image;
import java.awt.Rectangle;

import javax.swing.JFrame;

public class NormalWindowFrame extends WindowFrame {

	private JFrame frame;

	public NormalWindowFrame(JFrame frame) {
		super(frame);
		this.frame = frame;
	}

	@Override
	public Container getContentPane() {
		return frame.getContentPane();
	}

	@Override
	public Rectangle getMaximizedBounds() {
		return frame.getMaximizedBounds();
	}

	@Override
	public void setUndecorated(boolean b) {
		frame.setUndecorated(b);
	}

	@Override
	public void setState(int normal) {
		frame.setState(normal);
	}

	@Override
	public void setMaximizedBounds(Rectangle fullScreenBounds) {
		frame.setMaximizedBounds(fullScreenBounds);
	}

	@Override
	public int getExtendedState() {
		return frame.getExtendedState();
	}

	@Override
	public void setExtendedState(int i) {
		frame.setExtendedState(i);
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
