package com.ncc.savior.desktop.sidebar;

import java.awt.FlowLayout;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.ncc.savior.desktop.virtues.VirtueService;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;

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

	public void addApplication(ApplicationDefinition ad, VirtueApplicationItem va) throws IOException {
		container.add(va.getTileContainer());
		tiles.put(ad, va.getTileContainer());
	}

	public void renderTiles() {
		for (JPanel panel : tiles.values()) {
			container.add(panel);
		}
	}

	// public void addApplication(ApplicationDefinition ad, DesktopVirtue virtue,
	// VirtueContainer vc, FavoritesView fv)
	// throws IOException {
	// JPanel tile = new JPanel();
	// tile.setPreferredSize(new Dimension(90, 90));
	// tile.setBackground(Color.WHITE);
	// JLabel appName = new JLabel(ad.getName());
	// appName.setFont(new Font("Tahoma", Font.PLAIN, 11));
	// appName.setHorizontalAlignment(SwingConstants.CENTER);
	// tile.setLayout(new BorderLayout(0, 0));
	//
	// ImageIcon imageIcon = new
	// ImageIcon(AppsTile.class.getResource("/images/Test.png"));
	// Image image = imageIcon.getImage(); // transform it
	// Image newimg = image.getScaledInstance(47, 50, java.awt.Image.SCALE_SMOOTH);
	// // scale it the smooth way
	// imageIcon = new ImageIcon(newimg); // transform it back
	//
	// JLabel appImage = new JLabel(imageIcon);
	// tile.add(appImage, BorderLayout.CENTER);
	// tile.add(appName, BorderLayout.SOUTH);
	//
	// JLabel addFavorites = new JLabel();
	// addFavorites.setIcon(new
	// ImageIcon(AppsList.class.getResource("/images/add-to-favorites.png")));
	// addFavorites.setHorizontalAlignment(SwingConstants.LEFT);
	// tile.add(addFavorites, BorderLayout.NORTH);
	//
	// addListener(tile, vc, fv, ad, virtue, addFavorites);
	//
	// tiles.put(ad.getName(), tile);
	//
	// container.validate();
	// container.repaint();
	// container.add(tile);
	// }

}
