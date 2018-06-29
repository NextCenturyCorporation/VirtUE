package com.ncc.savior.desktop.sidebar;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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

	public VirtueTileContainer(DesktopVirtue virtue, VirtueService virtueService,
			Color headerColor, Color bodyColor, JScrollPane sp, JTextField textField) throws IOException {
		super(virtue, virtueService, sp, textField);
		dropDown = true;

		container = new JPanel();
		container.setLayout(new BorderLayout(0, 0));

		this.bodyColor = bodyColor;

		createContainer(virtue, headerColor, Color.GRAY, numRows);
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
		container.add(header, BorderLayout.NORTH);
		header.setLayout(new GridBagLayout());
		header.setBackground(headerColor);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.WEST;
		gbc.weightx = 1.0;
		gbc.gridx = 0;
		gbc.gridy = 0;

		JLabel title = new JLabel(dv.getName());
		title.setHorizontalAlignment(SwingConstants.CENTER);
		title.setForeground(new Color(255, 255, 255));
		title.setFont(new Font("Tahoma", Font.PLAIN, 15));
		header.add(title, gbc);

		GridBagConstraints gbc2 = new GridBagConstraints();
		gbc2.anchor = GridBagConstraints.WEST;
		gbc2.weightx = 1.0;
		gbc2.gridx = 1;
		gbc2.gridy = 0;
		this.statusLabel = new JLabel(this.status.toString());
		statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
		statusLabel.setForeground(new Color(255, 255, 255));
		header.add(statusLabel, gbc2);

		GridBagConstraints gbc3 = new GridBagConstraints();
		gbc3.gridx = 2;
		gbc3.gridy = 0;
		optionsLabel = new JLabel();
		optionsLabel.setHorizontalAlignment(SwingConstants.CENTER);
		ImageIcon optionsIcon = new ImageIcon(VirtueTileContainer.class.getResource("/images/options.png"));
		Image optionsImage = optionsIcon.getImage(); // transform it
		Image newOptionsImg = optionsImage.getScaledInstance(24, 24, java.awt.Image.SCALE_SMOOTH);
		optionsIcon = new ImageIcon(newOptionsImg);
		optionsLabel.setIcon(optionsIcon);
		header.add(optionsLabel, gbc3);

		addOptionsListener();

		header.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if (!dropDown) {
					dropDown = true;
					String keyword = textField.getText();
					search(null, va -> va.getApplicationName().toLowerCase().contains(keyword.toLowerCase()));
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
		tileContainer.setBorder(new EmptyBorder(10, 25, 10, 25));
		updateVirtue(virtue);
		numRows++;
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
			tileContainer.setBackground(Color.GRAY);
		}
	}

	@Override
	public int compareTo(VirtueTileContainer vc) {
		return headerTitle.compareTo(vc.getName());
	}
}
