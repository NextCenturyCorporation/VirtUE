package com.ncc.savior.desktop.sidebar.virtueapp;

import java.awt.Color;
import java.awt.Component;
import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;

import org.apache.commons.lang3.tuple.Pair;

import com.ncc.savior.desktop.virtues.IIconService;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.VirtueState;
import com.ncc.savior.virtueadmin.model.desktop.DesktopVirtue;

public class VirtueAndAppListCellRenderer implements ListCellRenderer<Pair<DesktopVirtue, ApplicationDefinition>> {
	private IIconService iconService;

	public VirtueAndAppListCellRenderer(IIconService iconService) {
		this.iconService = iconService;
	}

	@Override
	public Component getListCellRendererComponent(JList<? extends Pair<DesktopVirtue, ApplicationDefinition>> list,
			Pair<DesktopVirtue, ApplicationDefinition> value, int index, boolean isSelected, boolean cellHasFocus) {
		DesktopVirtue v = value.getLeft();
		VirtueState state = v.getVirtueState();
		Color bgColor = Color.RED;
		switch (state) {
		// ok now category
		case RUNNING:
			bgColor = getColor(isSelected, cellHasFocus, Color.green);
			break;
		// ok soon category
		case PAUSED:
		case PAUSING:
		case RESUMING:
		case CREATING:
		case LAUNCHING:
			bgColor = getColor(isSelected, cellHasFocus, Color.yellow);
			break;
		// ok in a while category
		case STOPPED:
		case UNPROVISIONED:
			bgColor = getColor(isSelected, cellHasFocus, Color.orange);
			break;
		// no good category
		case DELETED:
		case STOPPING:
		case DELETING:
		case ERROR:
		default:
			bgColor = getColor(isSelected, cellHasFocus, Color.red);
			break;

		}

		String iconKey = value.getRight().getIconKey();
		Image image = iconService.getImageNow(iconKey);
		JLabel label;
		if (image == null) {
			label = new JLabel(value.getRight().getName() + " - " + value.getLeft().getName());
		} else {
			image = image.getScaledInstance(24, 24, java.awt.Image.SCALE_SMOOTH);
			ImageIcon icon = new ImageIcon(image);
			label = new JLabel(value.getRight().getName() + " - " + value.getLeft().getName(), icon,
					SwingConstants.LEFT);
		}
		label.setOpaque(true);
		Component component = label;
		component.setBackground(bgColor);

		return component;
	}

	private Color getColor(boolean isSelected, boolean cellHasFocus, Color baseColor) {
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
