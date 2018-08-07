package com.ncc.savior.desktop.sidebar;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.virtues.VirtueService;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.VirtueState;
import com.ncc.savior.virtueadmin.model.desktop.DesktopVirtue;

/**
 * This represents a Virtue in the sidebar menu. It controls the sub menu when
 * the virtue is selected.
 *
 */
public class VirtueTileContainer extends AbstractVirtueContainer implements Comparable<VirtueTileContainer> {
	private static Logger logger = LoggerFactory.getLogger(VirtueTileContainer.class);

	private JPanel tileContainer;
	private Color bodyColor;

	private VirtueTile vt;

	public VirtueTileContainer(DesktopVirtue virtue, VirtueService virtueService,
			Color headerColor, Color bodyColor, JScrollPane sp, JTextField textField, GhostText ghostText,
			VirtueTile vt)
			throws IOException {
		super(virtue, virtueService, sp, textField, ghostText);
		dropDown = true;

		container = new JPanel();
		container.setLayout(new BorderLayout(0, 0));

		this.vt = vt;
		this.bodyColor = bodyColor;

		createContainer(virtue, headerColor, Color.LIGHT_GRAY, numRows);
		logger.debug("loaded");
	}

	public void addApplication(ApplicationDefinition ad, VirtueApplicationItem va) {
		tiles.put(ad.getId() + va.getVirtue().getTemplateId(), va);

		tileContainer.add(va.getContainer());
	}

	private void createContainer(DesktopVirtue dv, Color headerColor, Color bodyColor, int row) {
		this.row = row;
		container = new JPanel();
		container.setLayout(new BorderLayout(0, 0));

		this.header = new JPanel();
		header.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
		container.add(header, BorderLayout.NORTH);
		header.setLayout(new GridBagLayout());
		header.setBackground(headerColor);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.WEST;
		gbc.weightx = 1.0;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets = new Insets(0, 5, 0, 0);

		JLabel title = new JLabel(dv.getName());
		title.setHorizontalAlignment(SwingConstants.CENTER);
		title.setForeground(new Color(255, 255, 255));
		title.setFont(new Font("Tahoma", Font.PLAIN, 15));
		header.add(title, gbc);

		GridBagConstraints gbc2 = new GridBagConstraints();
		gbc2.anchor = GridBagConstraints.EAST;
		gbc2.weightx = 1.0;
		gbc2.gridx = 1;
		gbc2.gridwidth = 1;
		gbc2.gridy = 0;

		this.statusLabel = new JLabel(this.status.toString());
		statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
		statusLabel.setForeground(new Color(255, 255, 255));
		statusLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 35));

		JPanel statusOptionsContainer = new JPanel();
		statusOptionsContainer.setBackground(headerColor);
		statusOptionsContainer.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));

		header.add(statusOptionsContainer, gbc2);

		optionsLabel = new JLabel();
		optionsLabel.setHorizontalAlignment(SwingConstants.CENTER);
		ImageIcon optionsIcon = new ImageIcon(scaledOptionsImage);
		optionsLabel.setIcon(optionsIcon);
		optionsLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 9));

		statusOptionsContainer.add(statusLabel);
		statusOptionsContainer.add(optionsLabel);

		addOptionsListener();

		header.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if (!dropDown) {
					dropDown = true;
					String keyword = textField.getText();
					if (ghostText.getIsVisible()) {
						keyword = "";
					}
					dropDownSearch(keyword);
					tileContainer.validate();
					tileContainer.repaint();
					sp.validate();
					sp.getViewport().revalidate();

				} else {
					tileContainer.removeAll();
					tileContainer.validate();
					tileContainer.repaint();
					dropDown = false;
					sp.validate();
					sp.getViewport().revalidate();
				}
			}
		});

		this.tileContainer = new JPanel();
		tileContainer.setBackground(bodyColor);
		container.add(tileContainer, BorderLayout.CENTER);
		tileContainer.setLayout(new ModifiedFlowLayout(FlowLayout.CENTER, 20, 20));
		tileContainer.setBorder(new EmptyBorder(0, 25, 20, 25));
		updateVirtue(virtue);
		numRows++;
	}

	public void dropDownSearch(String keyword) {
		search(null, va -> va.getApplicationName().toLowerCase().contains(keyword.toLowerCase()));
	}

	public void search(Comparator<VirtueApplicationItem> comp, Predicate<VirtueApplicationItem> predicate) {
		if (dropDown) {
			tileContainer.removeAll();
			Collection<VirtueApplicationItem> vai = tiles.values();
			List<VirtueApplicationItem> matchedTiles;

			if (predicate != null) {
				matchedTiles = vai.stream().filter(predicate).collect(Collectors.toList());
			} else {
				matchedTiles = vai.stream().collect(Collectors.toList());
			}

			if (comp != null) {
				Collections.sort(matchedTiles, comp);
			} else {
				Collections.sort(matchedTiles);
			}

			for (VirtueApplicationItem va : matchedTiles) {
				tileContainer.add(va.getContainer());
			}

			container.validate();
			container.repaint();
		}
	}

	@Override
	public void updateVirtue(DesktopVirtue virtue) {
		this.virtue = virtue;
		this.statusLabel.setText(virtue.getVirtueState().toString());
		if (virtue.getVirtueState() == VirtueState.RUNNING) {
			tileContainer.setBackground(bodyColor);
		} else {
			tileContainer.setBackground(Color.LIGHT_GRAY);
		}
		search(null, null);
	}

	@Override
	public int compareTo(VirtueTileContainer vc) {
		return headerTitle.compareTo(vc.getName());
	}
}
