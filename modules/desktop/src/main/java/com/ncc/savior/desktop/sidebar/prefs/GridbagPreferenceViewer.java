package com.ncc.savior.desktop.sidebar.prefs;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Label;
import java.awt.Point;
import java.io.IOException;
import java.util.List;
import java.util.prefs.BackingStoreException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.MatteBorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.alerting.BaseAlertMessage;
import com.ncc.savior.desktop.alerting.PlainAlertMessage;
import com.ncc.savior.desktop.alerting.UserAlertingServiceHolder;

/**
 * Basic Preference viewer GUI used to view and clear preferences. This
 * implementation uses {@link GridBagLayout} to generate a table like view.
 */
public class GridbagPreferenceViewer {
	private static final Logger logger = LoggerFactory.getLogger(GridbagPreferenceViewer.class);

	private JDialog dialog;
	private PreferenceService preferenceService;

	public GridbagPreferenceViewer(PreferenceService preferenceService) {
		this.preferenceService = preferenceService;
	}

	public void displayPreferences() {
		if (dialog != null) {
			dialog.dispose();
		}
		this.dialog = new JDialog();

		List<DesktopPreferenceData> data = null;
		try {

			dialog.setTitle("Preferences");
			dialog.setAlwaysOnTop(true);
			GridBagLayout dialogGbl = new GridBagLayout();
			dialog.setLayout(dialogGbl);
			JPanel table = new JPanel();
			table.setLayout(new GridBagLayout());
			JScrollPane listScroll = new JScrollPane(table);
			JButton clearAllButton = new JButton("Clear All");
			clearAllButton.addActionListener((e) -> {
				clearAllPreferences();
			});
			table.setOpaque(false);
			// table.setBorder(BorderFactory.createEmptyBorder());
			// table.setBorder(BorderFactory.createLineBorder(Color.BLACK));

			int i = 1;
			addHeaders(table);
			for (DesktopPreference node : DesktopPreference.values()) {
				DesktopPreferenceDetails desc = preferenceService.getDescription(node);
				if (desc.isDisplayInPrefTable()) {
					if (desc.isNodeCollection()) {
						String[] elements = preferenceService.getCollectionElements(node);
						for (String element : elements) {
							data = preferenceService.getPreferenceData(node, element);
							i += createSingleRow(table, node, element, data, i);
						}
					} else {
						data = preferenceService.getPreferenceData(node, null);
						i += createSingleRow(table, node, "", data, i);
					}
				}
			}
			int width;
			if (i == 1) {
				// No prefs to show
				table.removeAll();
				addNoPrefLabel();
				width = 400;
			} else {
				// gridbag
				GridBagConstraints scrollGbc = new GridBagConstraints();
				scrollGbc.fill = GridBagConstraints.BOTH;
				scrollGbc.gridx = 0;
				scrollGbc.gridy = 1;
				scrollGbc.gridheight = 1;
				scrollGbc.gridwidth = 2;
				scrollGbc.weightx = 1;
				scrollGbc.weighty = 1;
				// scrollGbc.ipadx = 2;
				// scrollGbc.ipady = 2;
				scrollGbc.insets = new Insets(5, 5, 5, 5);

				GridBagConstraints clearAllGbc = new GridBagConstraints();
				// checkGbc.fill = GridBagConstraints.BOTH;
				clearAllGbc.gridx = 1;
				clearAllGbc.gridy = 2;
				clearAllGbc.weightx = 2;
				clearAllGbc.insets = new Insets(5, 55, 5, 5);

				listScroll.setBorder(BorderFactory.createEmptyBorder());
				listScroll.setMinimumSize(new Dimension(100, 300));
				dialog.add(listScroll, scrollGbc);
				dialog.add(clearAllButton, clearAllGbc);
				// dialog.add(openButton, openGbc);

				listScroll.setBorder(BorderFactory.createEmptyBorder());
				listScroll.setMinimumSize(new Dimension(100, 300));
				// dialog.setSize(new Dimension(600, 400));
				width = 600;
			}
			dialog.pack();
			Dimension size = dialog.getSize();
			size.width = width;

			dialog.setSize(size);
			dialog.setVisible(true);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		} catch (BackingStoreException e1) {
			reportError("Error attempting to get all Preferences", e1);
		}
	}

	private void addNoPrefLabel() {
		Label label = new Label("No preferences set.");
		dialog.add(label);
	}

	private void addHeaders(JPanel table) {
		JLabel pathLabel = new JLabel("Name");
		JLabel descLabel = new JLabel("Description");
		JLabel keyLabel = new JLabel("Key");
		JLabel valueLabel = new JLabel("Value");
		JLabel clearLabel = new JLabel("");
		pathLabel.setHorizontalAlignment(SwingConstants.CENTER);
		descLabel.setHorizontalAlignment(SwingConstants.CENTER);
		keyLabel.setHorizontalAlignment(SwingConstants.CENTER);
		valueLabel.setHorizontalAlignment(SwingConstants.CENTER);
		clearLabel.setHorizontalAlignment(SwingConstants.CENTER);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.ipadx = 5;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridwidth = 1;
		gbc.gridy = 0;
		gbc.weightx = 1;

		gbc.gridx = 0;
		table.add(pathLabel, gbc);

		gbc.gridx = 1;
		table.add(descLabel, gbc);

		// gbc.gridx = 3;
		// table.add(keyLabel, gbc);
		//
		// gbc.gridx = 4;
		// table.add(valueLabel, gbc);

		gbc.gridx = 2;
		table.add(clearLabel, gbc);
	}

	private int createSingleRow(JPanel table, DesktopPreference node, String element, List<DesktopPreferenceData> data,
			int i) {
		if (data.isEmpty()) {
			return 0;
		}
		int topBorder = 0;
		if (i == 1) {
			topBorder = 1;
		}
		int rightBorder = 0;
		int leftBorder = 1;
		int bottomBorder = 1;
		DesktopPreferenceDetails desc = preferenceService.getDescription(node);
		String name = desc.getName();
		if (desc.isNodeCollection() && element != null) {
			name += element;
		}
		JLabel nameLabel = new JLabel(name);
		JLabel descriptionLabel = new JLabel(desc.getDescription());

		JButton clearButton = new JButton("Clear");
		nameLabel.setHorizontalAlignment(SwingConstants.CENTER);
		descriptionLabel.setHorizontalAlignment(SwingConstants.CENTER);
		clearButton.setSize(100, 50);
		clearButton.addActionListener((a) -> {
			try {
				preferenceService.clearPreference(node, null);
				Dimension d = dialog.getSize();
				Point p = dialog.getLocation();
				dialog.dispose();
				displayPreferences();
				dialog.setSize(d);
				dialog.setLocation(p);
			} catch (BackingStoreException e) {
				reportError("Error clearing prefence", e);
			}
		});
		MatteBorder border = BorderFactory.createMatteBorder(topBorder, leftBorder, bottomBorder, rightBorder,
				Color.black);
		nameLabel.setBorder(border);
		descriptionLabel.setBorder(border);
		// clearButton.setBorder(BorderFactory.createLineBorder(Color.black));

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.ipadx = 5;
		gbc.ipady = 5;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridwidth = 1;
		gbc.gridy = i + 1;
		gbc.weightx = 25;
		gbc.gridheight = data.size();

		gbc.gridx = 0;
		table.add(nameLabel, gbc);

		gbc.gridx = 1;
		table.add(descriptionLabel, gbc);

		gbc.gridx = 2;
		gbc.weightx = 10;
		table.add(clearButton, gbc);

		int x = 0;
		for (DesktopPreferenceData row : data) {

			topBorder = (x == 0 ? topBorder : 0);
			bottomBorder = (x == data.size() - 1 ? bottomBorder : 1);

			JLabel valueLabel = new JLabel(row.getValue());
			valueLabel.setHorizontalAlignment(SwingConstants.CENTER);
			gbc.gridx = 4;
			gbc.gridy = i + 1 + x;
			gbc.gridheight = 1;
			valueLabel.setBorder(
					BorderFactory.createMatteBorder(topBorder, leftBorder, bottomBorder, rightBorder, Color.black));
			// table.add(valueLabel, gbc);

			JLabel keyLabel = new JLabel(row.getKey());
			keyLabel.setHorizontalAlignment(SwingConstants.CENTER);
			gbc.gridx = 3;
			keyLabel.setBorder(
					BorderFactory.createMatteBorder(topBorder, leftBorder, bottomBorder, rightBorder, Color.black));
			// table.add(keyLabel, gbc);

			x++;

		}
		return x + 1;
	}

	private void reportError(String msg, Exception e) {
		logger.error(msg);

		BaseAlertMessage alertMessage = new PlainAlertMessage("Preference Error",
				msg + " - " + e.getLocalizedMessage());
		try {
			UserAlertingServiceHolder.sendAlert(alertMessage);
		} catch (IOException e1) {
			logger.error("Error sending alert=" + alertMessage, e);
		}
	}

	private void clearAllPreferences() {
		try {
			preferenceService.clearAllPreferences();
			dialog.dispose();
			dialog = null;
		} catch (BackingStoreException e) {
			reportError("Error clearing all preferences", e);
		}
	}

}
