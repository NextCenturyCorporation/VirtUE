package com.ncc.savior.desktop.sidebar.defaultapp;

import java.awt.Color;

import com.ncc.savior.virtueadmin.model.VirtueState;

/**
 * Handles which color could be associated with a given status.
 *
 */
public class StatusColor {
	public static Color getColor(VirtueState state, boolean isSelected, boolean hasFocus) {
		Color yellow = new Color(170, 170, 50);
		Color green = new Color(0, 100, 0);
		Color orange = new Color(204, 84, 0);
		Color red = new Color(150, 0, 0);
		Color statusColor = Color.RED;

		switch (state) {
		// ok now category
		case RUNNING:
			statusColor = getColor(isSelected, hasFocus, green);
			break;
		// ok soon category
		case PAUSED:
		case PAUSING:
		case RESUMING:
		case CREATING:
		case LAUNCHING:
			statusColor = getColor(isSelected, hasFocus, yellow);
			break;
		// ok in a while category
		case STOPPED:
		case UNPROVISIONED:
			statusColor = getColor(isSelected, hasFocus, orange);
			break;
		// no good category
		case DELETED:
		case STOPPING:
		case DELETING:
		case ERROR:
		default:
			statusColor = getColor(isSelected, hasFocus, red);
			break;

		}
		return statusColor;
	}

	private static Color getColor(boolean isSelected, boolean cellHasFocus, Color baseColor) {
		boolean highlight = isSelected || cellHasFocus;
		float highlightPercent = .8f;
		if (highlight) {
			int r = (int) (baseColor.getRed() * highlightPercent);
			int g = (int) (baseColor.getGreen() * highlightPercent);
			int b = (int) (baseColor.getBlue() * highlightPercent);
			Color c = new Color(r, g, b);
			return c;
		} else {
			return baseColor;
		}

	}
}
