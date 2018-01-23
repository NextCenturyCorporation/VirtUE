package com.ncc.savior.desktop.xpra.application.swing;

import java.awt.Container;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Window;

import javax.swing.JDialog;
import javax.swing.JFrame;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link JDialog} based implementation of {@link WindowFrame}. This is used for
 * Dialogs and Modals.
 *
 *
 */
public class DialogWindowFrame extends WindowFrame {
	private static Logger logger = LoggerFactory.getLogger(DialogWindowFrame.class);

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

	@Override
	protected Window doSwitchToFullscreen(Window window, Window previous) {
		logger.warn("Cannot DialogWindow to fullscreen");
		return window;
	}

	@Override
	protected Window doSwitchFromFullscreen(Window window, Window previous) {
		logger.warn("Cannot DialogWindow to fullscreen");
		return window;
	}
}
