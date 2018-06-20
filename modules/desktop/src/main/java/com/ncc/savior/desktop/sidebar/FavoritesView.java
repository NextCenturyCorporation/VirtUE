package com.ncc.savior.desktop.sidebar;

import java.awt.FlowLayout;
import java.beans.PropertyChangeListener;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JScrollPane;

import com.ncc.savior.desktop.virtues.VirtueService;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.desktop.DesktopVirtue;

/**
 * This is the favorites component that can be set as the view to the sidebar
 * scrollPane
 */

public class FavoritesView extends AbstractAppsView {

	public FavoritesView(VirtueService virtueService) throws IOException {
		super(virtueService);
		container.setLayout(new ModifiedFlowLayout(FlowLayout.CENTER, 20, 20));

		container.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 10));
		container.validate();
		container.repaint();
	}

	public void addFavorite(ApplicationDefinition ad, DesktopVirtue virtue, VirtueContainer vc, JScrollPane sp,
			PropertyChangeListener listener) {
		if (tiles.get(ad.getId()) == null) {
			VirtueApplicationItem va = new VirtueApplicationItem(ad, virtueService, sp, vc, virtue, this, listener);
			va.tileSetup();
			va.setToFavorited();

			container.add(va.getContainer());
			tiles.put(ad.getId(), va);
		}
	}

	public void removeFavorite(ApplicationDefinition ad) {
		if (tiles != null && tiles.get(ad.getId()) != null) {
			container.remove(tiles.get(ad.getId()).getContainer());
			container.validate();
			container.repaint();
			tiles.remove(ad.getId());
		}
	}
}
