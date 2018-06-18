package com.ncc.savior.desktop.sidebar;

import java.util.HashMap;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.ncc.savior.desktop.virtues.VirtueService;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;

/**
 *
 * This is a superclass for appsList, appsTile, and favoritesView and it
 * includes the basic common components of each different view
 *
 */

public abstract class AbstractAppsView {

	protected JPanel container;
	protected VirtueService virtueService;
	protected JScrollPane sp;
	protected HashMap<ApplicationDefinition, JPanel> tiles;

	public AbstractAppsView(VirtueService vs, JScrollPane sp) {
		this.container = new JPanel();
		this.virtueService = vs;
		this.sp = sp;
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
