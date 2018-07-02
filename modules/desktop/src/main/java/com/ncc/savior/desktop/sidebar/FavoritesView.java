package com.ncc.savior.desktop.sidebar;

import java.awt.FlowLayout;
import java.awt.Image;
import java.io.IOException;
import java.util.Comparator;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

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

	public void addFavorite(ApplicationDefinition ad, DesktopVirtue virtue, VirtueApplicationItem va,
			JTextField textField, Comparator<VirtueApplicationItem> comp) {
		if (tiles.get(ad.getId() + virtue.getTemplateId()) == null) {
			favorites.putBoolean(ad.getId() + virtue.getTemplateId(), true);

			container.add(va.getContainer());
			tiles.put(ad.getId() + virtue.getTemplateId(), va);
		}
		String keyword = textField.getText();
		search(keyword, comp, currVa -> currVa.getApplicationName().toLowerCase().contains(keyword.toLowerCase()));
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

	public void setTileImage(ApplicationDefinition ad, DesktopVirtue virtue, Image image) {
		VirtueApplicationItem va = tiles.get(ad.getId() + virtue.getTemplateId());
		if (va != null) {
			va.setTileImage(image);
		}
	}

	@Override
	public void addTile(VirtueApplicationItem va) {
		container.add(va.getContainer());
	}
}
