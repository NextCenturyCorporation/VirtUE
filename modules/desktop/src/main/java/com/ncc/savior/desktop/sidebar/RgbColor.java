package com.ncc.savior.desktop.sidebar;

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
}
