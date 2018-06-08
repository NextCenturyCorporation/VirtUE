package com.ncc.savior.desktop.sidebar;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;

public class VirtueList {
	private JPanel container;
	public static boolean dropDown = false;

	public VirtueList() throws IOException {
		this.container = new JPanel();
		GridBagLayout gbl_container = new GridBagLayout();
		gbl_container.columnWidths = new int[] { 584, 0 };
		gbl_container.rowHeights = new int[] { 837, 0 };
		gbl_container.columnWeights = new double[] { 0.0, Double.MIN_VALUE };
		gbl_container.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		container.setLayout(gbl_container);

		// Entire grid of Artist, Browsers, Math
		JPanel headerContainer = new JPanel();
		headerContainer.setPreferredSize(new Dimension(200, 200));
		headerContainer.setSize(new Dimension(200, 200));

		// Artist, Browsers, Math
		JPanel tile = new JPanel();
		tile.setBorder(new LineBorder(new Color(128, 128, 128), 1));
		tile.setBackground(Color.ORANGE);
		JLabel lblNewLabel_1 = new JLabel("Firefox");
		lblNewLabel_1.setIcon(new ImageIcon(AppsList.class.getResource("/images/play.png")));
		lblNewLabel_1.setHorizontalAlignment(SwingConstants.LEFT);
		tile.setLayout(new BoxLayout(tile, BoxLayout.X_AXIS));
		tile.add(lblNewLabel_1);
		headerContainer.setBorder(new LineBorder(new Color(128, 128, 128), 1));
		headerContainer.setLayout(new BorderLayout(0, 0));
		headerContainer.add(tile, BorderLayout.NORTH);

		GridBagConstraints gbc_headerContainer = new GridBagConstraints();
		gbc_headerContainer.weighty = 0.1;
		gbc_headerContainer.weightx = 0.1;
		gbc_headerContainer.ipadx = 20;
		gbc_headerContainer.ipady = 20;
		gbc_headerContainer.fill = GridBagConstraints.BOTH;
		gbc_headerContainer.gridx = 0;
		gbc_headerContainer.gridy = 0;
		container.add(headerContainer, gbc_headerContainer);

		tile.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if (!dropDown) {
					dropDown = true;
					try {
						for (Integer i = 0; i < 6; i++) {
							addTile(i.toString(), headerContainer);
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
					headerContainer.validate();
					headerContainer.repaint();
				} else {
					dropDown = false;
					headerContainer.removeAll();
					headerContainer.validate();
					headerContainer.repaint();
					headerContainer.add(tile);
				}
			}
		});
	}

	public void addTile(String name, JPanel headerContainer) throws IOException {
		JPanel tile = new JPanel();
		tile.setBorder(new LineBorder(new Color(128, 128, 128), 1));
		tile.setBackground(Color.WHITE);
		JLabel lblNewLabel_1 = new JLabel(name);
		lblNewLabel_1.setIcon(new ImageIcon(AppsList.class.getResource("/images/stop.png")));
		lblNewLabel_1.setHorizontalAlignment(SwingConstants.LEFT);
		tile.setLayout(new BoxLayout(tile, BoxLayout.X_AXIS));
		tile.add(lblNewLabel_1);
		headerContainer.setBorder(new LineBorder(new Color(128, 128, 128), 1));
		headerContainer.setLayout(new GridLayout(0, 1, 0, 0));
		headerContainer.add(tile);
	}

	// public void addHeader(String name, JPanel header) {
	// header.setBorder(new LineBorder(new Color(128, 128, 128), 1));
	// header.setBackground(Color.WHITE);
	// header.setLayout(new GridLayout(0, 1, 0, 0));
	// JLabel copyLabel = new JLabel(name);
	// copyLabel.setIcon(new
	// ImageIcon(AppsList.class.getResource("/images/play.png")));
	// copyLabel.setHorizontalAlignment(SwingConstants.LEFT);
	// }

	public static void main(String[] args) throws IOException {
		VirtueList vt = new VirtueList();
		JFrame frame = new JFrame("FrameDemo");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(vt.getContainer());
		frame.setSize(600, 875);
		frame.setVisible(true);
	}

	public JPanel getContainer() {
		return container;
	}

	public void setDropDownToFalse() {
		dropDown = false;
	}

}
