package com.ncc.savior.desktop.xpra.application.swing;

import java.awt.Dimension;
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
		super.setPreferredSize(new Dimension(width, height));
		img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		// Graphics g = img.getGraphics();
		// g.setColor(new Color(0, 0, 0, 0));
		// g.fillRect(0, 0, width, height);
		// g.dispose();
	}

	@Override
	protected void paintComponent(Graphics g) {
		// super.paintComponent(g);
		// g.setColor(Color.red);
		// g.drawRect(0, 0, this.getWidth() - 1, this.getHeight() - 1);
		g.drawImage(img, 0, 0, null);

		g.dispose();

	}

	@Override
	public Graphics getGraphics() {
		return img.getGraphics();
	}

	public void setsize(int width, int height) {
		int oldWidth = img.getWidth(null);
		int oldHeight = img.getHeight(null);
		if (oldWidth != width || oldHeight != height) {
			Image oldImg = img;
			BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			Graphics g = newImage.getGraphics();
			g.drawImage(oldImg, 0, 0, oldWidth, oldHeight, null);
			g.dispose();
			img = newImage;
			super.setPreferredSize(new Dimension(width, height));
		}
	}
}
