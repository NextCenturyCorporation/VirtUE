/*
 * Copyright (C) 2019 Next Century Corporation
 * 
 * This file may be redistributed and/or modified under either the GPL
 * 2.0 or 3-Clause BSD license. In addition, the U.S. Government is
 * granted government purpose rights. For details, see the COPYRIGHT.TXT
 * file at the root of this project.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 * 
 * SPDX-License-Identifier: (GPL-2.0-only OR BSD-3-Clause)
 */
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
