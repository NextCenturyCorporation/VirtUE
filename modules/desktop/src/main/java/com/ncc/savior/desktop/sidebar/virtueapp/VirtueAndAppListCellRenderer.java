package com.ncc.savior.desktop.sidebar.virtueapp;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

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
		ApplicationDefinition a = value.getRight();
		VirtueState state = v.getVirtueState();
		Color yellow = new Color(170, 170, 50);
		Color green = new Color(0, 100, 0);
		Color orange = new Color(204, 84, 0);
		Color red = new Color(150, 0, 0);
		Color statusColor = Color.RED;

		switch (state) {
		// ok now category
		case RUNNING:
			statusColor = getColor(isSelected, cellHasFocus, green);
			break;
		// ok soon category
		case PAUSED:
		case PAUSING:
		case RESUMING:
		case CREATING:
		case LAUNCHING:
			statusColor = getColor(isSelected, cellHasFocus, yellow);
			break;
		// ok in a while category
		case STOPPED:
		case UNPROVISIONED:
			statusColor = getColor(isSelected, cellHasFocus, orange);
			break;
		// no good category
		case DELETED:
		case STOPPING:
		case DELETING:
		case ERROR:
		default:
			statusColor = getColor(isSelected, cellHasFocus, red);
			break;

		}
		String iconKey = value.getRight().getIconKey();
		Image image = iconService.getImageNow(iconKey);
		JLabel virtueLabel = new JLabel(v.getName());
		JLabel status = new JLabel(v.getVirtueState().toString());

		JLabel appLabel;
		JLabel iconLabel = null;
		if (image != null) {
			image = image.getScaledInstance(48, 48, java.awt.Image.SCALE_SMOOTH);
			ImageIcon icon = new ImageIcon(image);
			iconLabel = new JLabel(icon);
		}
		appLabel = new JLabel(a.getName());
		status.setForeground(statusColor);
		GridBagLayout gbl = new GridBagLayout();

		GridBagConstraints gbcAppIcon = new GridBagConstraints();
		gbcAppIcon.gridx = 0;
		gbcAppIcon.gridy = 0;
		gbcAppIcon.weightx = 0;
		gbcAppIcon.weighty = 0;
		gbcAppIcon.gridheight = 3;
		gbcAppIcon.ipadx = 3;
		gbcAppIcon.ipady = 3;

		gbcAppIcon.anchor = GridBagConstraints.CENTER;
		GridBagConstraints gbcAppLabel = new GridBagConstraints();
		gbcAppLabel.gridx = 1;
		gbcAppLabel.gridy = 0;
		gbcAppLabel.weightx = 1;
		gbcAppLabel.weighty = 1;

		GridBagConstraints gbcVirtueLabel = new GridBagConstraints();
		gbcVirtueLabel.gridx = 1;
		gbcVirtueLabel.gridy = 1;
		gbcVirtueLabel.weightx = 1;
		gbcVirtueLabel.weighty = 1;
		GridBagConstraints gbcStatus = new GridBagConstraints();
		gbcStatus.gridx = 1;
		gbcStatus.gridy = 2;
		gbcStatus.weightx = 1;
		gbcStatus.weighty = 1;
		JPanel component = new JPanel(gbl);
		component.add(iconLabel, gbcAppIcon);
		component.add(appLabel, gbcAppLabel);
		component.add(virtueLabel, gbcVirtueLabel);
		component.add(status, gbcStatus);

		Color bg;
		if (isSelected) {
			component.setBorder(
					BorderFactory.createLoweredBevelBorder());
			// bg = new Color();
		} else {
			component.setBorder(BorderFactory.createRaisedBevelBorder());

		}
		bg = (index % 2 == 0 ? new Color(195, 195, 195) : new Color(225, 225, 225));
		bg = new Color(225, 225, 225);
		component.setBackground(bg);

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
