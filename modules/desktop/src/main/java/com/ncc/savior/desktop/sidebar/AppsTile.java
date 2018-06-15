package com.ncc.savior.desktop.sidebar;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import com.ncc.savior.desktop.virtues.VirtueService;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.desktop.DesktopVirtue;

/**
 * This is the application tile component that can be set as the view to the
 * sidebar scrollPane
 */

public class AppsTile extends AbstractAppsView {

	public AppsTile(VirtueService vs, JScrollPane sp) throws IOException {
		super(vs, sp);
		container.setLayout(new ModifiedFlowLayout(FlowLayout.CENTER, 20, 20));

		container.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		container.validate();
		container.repaint();
	}

	public void addTiles(DesktopVirtue virtue, VirtueContainer vc, FavoritesView fv) throws IOException {
		for (ApplicationDefinition ad : virtue.getApps().values()) {
			JPanel tile = new JPanel();
			tile.setPreferredSize(new Dimension(90, 90));
			tile.setBackground(Color.WHITE);
			JLabel appName = new JLabel(ad.getName());
			appName.setFont(new Font("Tahoma", Font.PLAIN, 11));
			appName.setHorizontalAlignment(SwingConstants.CENTER);
			tile.setLayout(new BorderLayout(0, 0));

			ImageIcon imageIcon = new ImageIcon(AppsTile.class.getResource("/images/Test.png"));
			Image image = imageIcon.getImage(); // transform it
			Image newimg = image.getScaledInstance(47, 50, java.awt.Image.SCALE_SMOOTH); // scale it the smooth way
			imageIcon = new ImageIcon(newimg); // transform it back

			JLabel appImage = new JLabel(imageIcon);
			tile.add(appImage);
			tile.add(appName, BorderLayout.SOUTH);
			tile.setSize(0, 40);

			addListener(tile, vc, fv, ad, virtue);

			tiles.put(ad.getName(), tile);

			container.validate();
			container.repaint();
			container.add(tile);
		}
	}

	public JPanel getContainer() {
		return container;
	}

}
