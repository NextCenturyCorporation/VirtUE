package com.ncc.savior.desktop.sidebar;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
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
import java.util.HashMap;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
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
public class VirtueContainer {
	private static Logger logger = LoggerFactory.getLogger(VirtueContainer.class);
	private DesktopVirtue virtue;
	private VirtueService virtueService;

	private String headerTitle;
	private VirtueState status;
	private JLabel statusLabel;
	private JPanel container;
	private JPanel tileContainer;
	private JPanel header;
	private JScrollPane sp;
	private Color bodyColor;

	private static int numRows = 0;
	private int row;

	private HashMap<String, JPanel> tiles;

	public VirtueContainer(DesktopVirtue virtue, VirtueService virtueService,
			Color headerColor, Color bodyColor, JScrollPane sp) throws IOException {
		this.virtueService = virtueService;
		this.virtue = virtue;
		this.tiles = new HashMap<String, JPanel>();
		this.headerTitle = virtue.getName();
		this.status = virtue.getVirtueState();
		this.sp = sp;
		this.bodyColor = bodyColor;
		createContainer(virtue, headerColor, Color.GRAY, numRows);

		logger.debug("loaded");
	}

	public void addApplication(ApplicationDefinition ad, VirtueApplicationItem va) {
		JPanel tile = va.getContainer();

		tiles.put(ad.getName(), tile);

		tileContainer.add(tile);
	}

	public void addTiles() throws IOException {
		for (ApplicationDefinition ad : virtue.getApps().values()) {
			JPanel tile = new JPanel();
			tile.setPreferredSize(new Dimension(90, 90));
			tile.setBackground(Color.WHITE);
			JLabel application = new JLabel(ad.getName());
			application.setFont(new Font("Tahoma", Font.PLAIN, 11));
			application.setHorizontalAlignment(SwingConstants.CENTER);
			tile.setLayout(new BorderLayout(0, 0));

			ImageIcon imageIcon = new ImageIcon(VirtueContainer.class.getResource("/images/Test.png"));
			Image image = imageIcon.getImage(); // transform it
			Image newimg = image.getScaledInstance(47, 50, java.awt.Image.SCALE_SMOOTH); // scale it the smooth way
			imageIcon = new ImageIcon(newimg); // transform it back

			JLabel picLabel = new JLabel(imageIcon);
			tile.add(picLabel);
			tile.add(application, BorderLayout.SOUTH);

			tile.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent event) {
					JPopupMenu pm = new JPopupMenu();
					JMenuItem mi1 = new JMenuItem("Yes");
					JMenuItem mi2 = new JMenuItem("No");
					JLabel prompt = new JLabel("Would you like to start a " + ad.getName() + " application?");
					pm.add(new JLabel("Would you like to start a " + ad.getName() + " application?"));
					prompt.setHorizontalAlignment(SwingConstants.CENTER);

					mi1.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent evt) {
							try {
								if (logger.isDebugEnabled()) {
									logger.debug("virtue started");
								}
								// virtueService.startVirtue(virtue);
								virtueService.startApplication(virtue, ad, new RgbColor(0, 0, 0, 0));
								// virtue.setVirtueState(VirtueState.LAUNCHING);
								// updateVirtue(virtue);
							} catch (IOException e) {
								String msg = "Error attempting to start virtue=" + virtue;
								logger.error(msg, e);
							}
						}
					});

					pm.setPopupSize(375, 75);
					pm.add(mi1);
					pm.add(mi2);
					pm.show(sp, 50, 150);
				}
			});

			tiles.put(ad.getName(), tile);
			tileContainer.add(tile);
		}
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

	public void removeVirtue(String name) {
		tileContainer.remove(tiles.get(name));
		tiles.remove(name);
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
}
