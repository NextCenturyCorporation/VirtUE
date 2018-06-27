package com.ncc.savior.desktop.sidebar;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import com.ncc.savior.desktop.virtues.VirtueService;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.desktop.DesktopVirtue;

public class VirtueListContainer extends AbstractVirtueContainer implements Comparable<VirtueListContainer> {

	public VirtueListContainer(DesktopVirtue virtue, VirtueService virtueService, Color headerColor,
			JScrollPane sp, JTextField textField) {
		super(virtue, virtueService, sp, textField);
		dropDown = false;

		this.container = new JPanel();
		container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

		createContainer(virtue, headerColor);
	}

	public void addApplication(ApplicationDefinition ad, VirtueApplicationItem va) {
		tiles.put(ad.getId() + va.getVirtue().getTemplateId(), va);
	}

	private void createContainer(DesktopVirtue dv, Color headerColor) {
		this.row = numRows;
		this.header = new JPanel();
		header.setSize(new Dimension(450, 70));
		header.setMinimumSize(new Dimension(450, 70));
		header.setMaximumSize(new Dimension(10000, 70));
		header.setPreferredSize(new Dimension(450, 70));
		container.add(header);

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
		Image optionsImage = optionsIcon.getImage();
		Image newOptionsImg = optionsImage.getScaledInstance(24, 24, java.awt.Image.SCALE_SMOOTH);
		optionsIcon = new ImageIcon(newOptionsImg);
		optionsLabel.setIcon(optionsIcon);
		header.add(optionsLabel, gbc3);

		container.add(header);

		addOptionsListener();

		container.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if (!dropDown) {
					dropDown = true;
					String keyword = textField.getText();
					search(null, va -> va.getApplicationName().toLowerCase().contains(keyword.toLowerCase()));
					container.validate();
					container.repaint();
					sp.validate();
					sp.getViewport().revalidate();
				} else {
					container.removeAll();
					container.add(header);
					container.validate();
					container.repaint();
					dropDown = false;
					sp.validate();
					sp.getViewport().revalidate();
				}
			}
		});

		numRows++;
	}

	public void search(Comparator<VirtueApplicationItem> comp, Predicate<VirtueApplicationItem> predicate) {
		container.removeAll();
		container.add(header);
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
			if (dropDown) {
				container.add(va.getContainer());
			}
		}

		container.validate();
		container.repaint();
	}

	@Override
	public void updateVirtue(DesktopVirtue virtue) {
		this.virtue = virtue;
		this.statusLabel.setText(virtue.getVirtueState().toString());
	}

	@Override
	public int compareTo(VirtueListContainer vlc) {
		return headerTitle.compareTo(vlc.getName());
	}

}
