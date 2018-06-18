package com.ncc.savior.desktop.sidebar;

import java.util.HashMap;

import javax.swing.JPanel;

import com.ncc.savior.virtueadmin.model.ApplicationDefinition;

/**
 *
 * This is a superclass for appsList, appsTile, and favoritesView and it
 * includes the basic common components of each different view
 *
 */

public abstract class AbstractAppsView {

	protected JPanel container;
	protected HashMap<ApplicationDefinition, JPanel> tiles;

	public AbstractAppsView() {
		this.container = new JPanel();
		this.tiles = new HashMap<ApplicationDefinition, JPanel>();
	}

	public void removeApplication(ApplicationDefinition ad) {
		container.remove(tiles.get(ad));
		container.validate();
		container.repaint();
		tiles.remove(ad);
		container.validate();
		container.repaint();
	}

	public JPanel getContainer() {
		return container;
	}

}
