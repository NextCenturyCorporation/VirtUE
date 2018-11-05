package com.ncc.savior.desktop.sidebar;

import java.awt.Color;

public class RgbColor {
	private double opacity;
	private double blue;
	private double green;
	private double red;

	public RgbColor(double red, double green, double blue, double opacity) {
		this.red = red;
		this.green = green;
		this.blue = blue;
		this.opacity = opacity;
	}

	public double getOpacity() {
		return opacity;
	}

	public double getBlue() {
		return blue;
	}

	public double getGreen() {
		return green;
	}

	public double getRed() {
		return red;
	}

	@Override
	public String toString() {
		return "RgbColor [opacity=" + opacity + ", blue=" + blue + ", green=" + green + ", red=" + red + "]";
	}

	public static RgbColor fromColor(Color color) {
		return new RgbColor(color.getRed() / 255.0, color.getGreen() / 255.0, color.getBlue() / 255.0,
				color.getAlpha() / 255.0);
	}
}
