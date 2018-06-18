package com.ncc.savior.desktop.sidebar;

import java.awt.FlowLayout;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.ncc.savior.desktop.virtues.VirtueService;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;

public class FavoritesView extends AbstractAppsView {

	public FavoritesView(VirtueService vs, JScrollPane sp) throws IOException {
		super();
		container.setLayout(new ModifiedFlowLayout(FlowLayout.CENTER, 20, 20));

		container.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 10));
		container.validate();
		container.repaint();
	}

	public void addFavorite(JPanel tile, ApplicationDefinition ad) {
		container.add(tile);
		tiles.put(ad, tile);
	}

	public void removeFavorite(ApplicationDefinition ad) {
		if (tiles != null) {
			container.remove(tiles.get(ad));
			container.validate();
			container.repaint();
			tiles.remove(ad);
		}
	}
}
