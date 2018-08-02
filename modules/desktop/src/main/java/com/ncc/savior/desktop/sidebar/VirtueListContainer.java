package com.ncc.savior.desktop.sidebar;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
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

	private VirtueList vl;

	public VirtueListContainer(DesktopVirtue virtue, VirtueService virtueService, Color headerColor,
			JScrollPane sp, JTextField textField, GhostText ghostText, VirtueList vl) {
		super(virtue, virtueService, sp, textField, ghostText);
		dropDown = false;

		this.vl = vl;
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
		header.setSize(new Dimension(450, 50));
		header.setMinimumSize(new Dimension(450, 50));
		header.setMaximumSize(new Dimension(10000, 50));
		header.setPreferredSize(new Dimension(450, 50));
		header.setBorder(BorderFactory.createMatteBorder(0, 1, 1, 1, Color.BLACK));
		container.add(header);

		header.setLayout(new GridBagLayout());
		header.setBackground(headerColor);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.WEST;
		gbc.weightx = 1.0;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets = new Insets(0, 15, 0, 0);

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

		container.add(header);

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
					container.validate();
					container.repaint();
					sp.validate();
					sp.getViewport().revalidate();
				} else {
					dropDown = false;
					container.removeAll();
					container.add(header);
					container.validate();
					container.repaint();
					sp.validate();
					sp.getViewport().revalidate();
				}
			}
		});

		numRows++;
	}

	public void dropDownSearch(String keyword) {
		search(null, va -> va.getApplicationName().toLowerCase().contains(keyword.toLowerCase()));
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
