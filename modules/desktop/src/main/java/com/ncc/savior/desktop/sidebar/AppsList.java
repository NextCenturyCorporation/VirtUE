package com.ncc.savior.desktop.sidebar;

import java.io.IOException;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.ncc.savior.desktop.virtues.VirtueService;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;

/**
 * This is the application list component that can be set as the view to the
 * sidebar scrollPane
 */

public class AppsList extends AbstractAppsView {

	public AppsList(VirtueService vs, JScrollPane sp) throws IOException {
		super(vs, sp);
		container.setLayout(new ModifiedFlowLayout());
	}

	public void addApplication(ApplicationDefinition ad, VirtueApplicationItem va) throws IOException {
		JPanel tile = va.getListContainer();

		tiles.put(ad, tile);

		container.validate();
		container.repaint();
		container.add(tile);
	}

	@Override
	public JPanel getContainer() {
		return container;
	}

	// public void addApplication(ApplicationDefinition ad, DesktopVirtue virtue,
	// VirtueContainer vc, FavoritesView fv)
	// throws IOException {
	// JPanel tile = new JPanel();
	// tile.setBorder(new LineBorder(Color.GRAY, 1));
	// tile.setBackground(Color.WHITE);
	// JLabel appImage = new JLabel(ad.getName());
	// appImage.setIcon(new
	// ImageIcon(AppsList.class.getResource("/images/play.png")));
	// appImage.setHorizontalAlignment(SwingConstants.LEFT);
	//
	// JLabel addFavorites = new JLabel();
	// addFavorites.setIcon(new
	// ImageIcon(AppsList.class.getResource("/images/add-to-favorites.png")));
	// addFavorites.setHorizontalAlignment(SwingConstants.LEFT);
	// tile.add(addFavorites, BorderLayout.NORTH);
	//
	// tile.setLayout(new BoxLayout(tile, BoxLayout.X_AXIS));
	// tile.add(appImage);
	// tile.setSize(new Dimension(475, 70));
	// tile.setMinimumSize(new Dimension(475, 70));
	// tile.setMaximumSize(new Dimension(475, 70));
	// tile.setPreferredSize(new Dimension(475, 70));
	//
	// addListener(tile, vc, fv, ad, virtue, addFavorites);
	//
	// tiles.put(ad.getName(), tile);
	//
	// container.add(tile);
	// container.validate();
	// container.repaint();
	// }

}
