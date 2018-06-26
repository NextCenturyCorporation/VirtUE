package com.ncc.savior.desktop.sidebar;

import java.awt.FlowLayout;
import java.awt.Image;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.prefs.Preferences;

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

	private Preferences favorites;

	public FavoritesView(VirtueService virtueService, JScrollPane sp, Preferences favorites) throws IOException {
		super(virtueService, sp);
		this.favorites = favorites;

		container.setLayout(new ModifiedFlowLayout(FlowLayout.CENTER, 20, 20));

		container.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 10));
		container.validate();
		container.repaint();
	}

	public void addFavorite(ApplicationDefinition ad, DesktopVirtue virtue, VirtueTileContainer vc, JScrollPane sp,
			PropertyChangeListener listener, Image image) {
		if (tiles.get(ad.getId() + virtue.getTemplateId()) == null) {
			VirtueApplicationItem va = new VirtueApplicationItem(ad, virtueService, sp, vc, virtue, this, listener,
					image, true);
			va.tileSetup();
			va.setToFavorited();

			favorites.putBoolean(ad.getId() + virtue.getTemplateId(), true);

			container.add(va.getContainer());
			tiles.put(ad.getId() + virtue.getTemplateId(), va);
		}
	}

	public void removeFavorite(ApplicationDefinition ad, DesktopVirtue virtue) {
		if (tiles != null && tiles.get(ad.getId() + virtue.getTemplateId()) != null) {
			container.remove(tiles.get(ad.getId() + virtue.getTemplateId()).getContainer());
			container.validate();
			container.repaint();

			favorites.remove(ad.getId() + virtue.getTemplateId());
			tiles.remove(ad.getId() + virtue.getTemplateId());
		}
	}
}
