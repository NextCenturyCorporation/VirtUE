package com.ncc.savior.desktop.xpra.application.swing;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JCanvas extends JComponent {
	private static final Logger logger = LoggerFactory.getLogger(JCanvas.class);

	Image img;
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public JCanvas(int width, int height) {
		super.setSize(width, height);
		img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics g = img.getGraphics();
		g.setColor(new Color(0, 0, 0, 0));
		g.fillRect(0, 0, width, height);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.setColor(Color.red);
		g.drawRect(0, 0, this.getWidth() - 1, this.getHeight() - 1);
		// Draw Text
		g.drawImage(img, 0, 0, null);

		g.dispose();

	}

	@Override
	public Graphics getGraphics() {
		return img.getGraphics();
	}

	public void setWidth(int width) {
		logger.warn("JCanvas resize not implemented yet!");
	}

	public void setHeight(int height) {
		logger.warn("JCanvas resize not implemented yet!");
	}
}
