package com.ncc.savior.desktop.sidebar;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
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
public class VirtueContainer implements Comparable<VirtueContainer> {
	private static Logger logger = LoggerFactory.getLogger(VirtueContainer.class);
	private DesktopVirtue virtue;
	private VirtueService virtueService;

	private String headerTitle;
	private VirtueState status;
	private JLabel statusLabel;
	private JPanel container;
	private JPanel tileContainer;
	private JPanel header;
	private Color bodyColor;

	private static int numRows = 0;
	private int row;

	private HashMap<String, VirtueApplicationItem> tiles;

	public VirtueContainer(DesktopVirtue virtue, VirtueService virtueService,
			Color headerColor, Color bodyColor) throws IOException {
		this.virtueService = virtueService;
		this.virtue = virtue;
		this.tiles = new HashMap<String, VirtueApplicationItem>();
		this.headerTitle = virtue.getName();
		this.status = virtue.getVirtueState();
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
		this.container = new JPanel();
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
		JLabel optionsLabel = new JLabel();
		optionsLabel.setHorizontalAlignment(SwingConstants.CENTER);
		ImageIcon optionsIcon = new ImageIcon(VirtueContainer.class.getResource("/images/options.png"));
		Image optionsImage = optionsIcon.getImage(); // transform it
		Image newOptionsImg = optionsImage.getScaledInstance(24, 24, java.awt.Image.SCALE_SMOOTH);
		optionsIcon = new ImageIcon(newOptionsImg);
		optionsLabel.setIcon(optionsIcon);
		header.add(optionsLabel, gbc3);

		optionsLabel.setToolTipText("Click to start or stop a virtue");

		optionsLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent event) {
				JPopupMenu pm = new JPopupMenu();
				JMenuItem mi1 = new JMenuItem("Stop");

				mi1.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent evt) {
						try {
							virtueService.stopVirtue(virtue);
							virtue.setVirtueState(VirtueState.STOPPING);
							updateVirtue(virtue);
						} catch (IOException e) {
							String msg = "Error attempting to stop virtue=" + virtue;
							logger.error(msg, e);
						}
					}
				});

				pm.setPopupSize(45, 38);
				pm.add(mi1);
				pm.show(optionsLabel, -20, 24);
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

	public int getRow() {
		return row;
	}

	public void setRow(int row) {
		this.row = row;
	}

	public static void resetRows() {
		numRows = 0;
	}

	public JPanel getContainer() {
		return container;
	}

	public DesktopVirtue getVirtue() {
		return virtue;
	}

	public String getName() {
		return headerTitle;
	}

	public boolean containsKeyword(String keyword) {
		for (ApplicationDefinition ad : virtue.getApps().values()) {
			if (ad.getName().toLowerCase().contains(keyword.toLowerCase())) {
				return true;
			}
		}
		return false;
	}

	public void search(Comparator<VirtueApplicationItem> comp,
			Predicate<VirtueApplicationItem> predicate) {
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

		tileContainer.validate();
		tileContainer.repaint();
	}

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
	public int compareTo(VirtueContainer vc) {
		return headerTitle.compareTo(vc.getName());
	}
}
