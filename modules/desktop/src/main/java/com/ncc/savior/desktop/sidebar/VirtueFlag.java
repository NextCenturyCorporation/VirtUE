package com.ncc.savior.desktop.sidebar;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;

import javax.swing.JPanel;

public class VirtueFlag extends JPanel {

	private static final long serialVersionUID = 1L;

	private Color color;

	public VirtueFlag(Color color) {
		this.color = color;
	}

	@Override
	public void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;

		Polygon triangle = new Polygon();
		triangle.addPoint(0, 0);
		triangle.addPoint(20, 0);
		triangle.addPoint(0, 20);

		g2.setColor(color);
		g2.fillPolygon(triangle);
		g2.drawPolygon(triangle);
	}
}