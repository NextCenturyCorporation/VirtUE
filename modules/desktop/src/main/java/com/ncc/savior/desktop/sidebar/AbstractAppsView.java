package com.ncc.savior.desktop.sidebar;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JPanel;

import com.ncc.savior.desktop.virtues.VirtueService;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;

/**
 *
 * This is a superclass for appsList, appsTile, and favoritesView and it
 * includes the basic common components of each different view
 *
 */

public abstract class AbstractAppsView {

	protected static Integer num = 0;

	protected VirtueService virtueService;
	protected JPanel container;
	protected HashMap<String, VirtueApplicationItem> tiles;

	public AbstractAppsView(VirtueService virtueService) {
		this.virtueService = virtueService;
		this.container = new JPanel();
		this.tiles = new HashMap<String, VirtueApplicationItem>();
	}

	public void addApplication(ApplicationDefinition ad, VirtueApplicationItem va) throws IOException {
		tiles.put(ad.getId(), va);
		num++;
		container.add(va.getContainer());

		container.validate();
		container.repaint();
	}

	public void removeApplication(ApplicationDefinition ad) {
		container.remove(tiles.get(ad.getId()).getContainer());
		container.validate();
		container.repaint();
		tiles.remove(ad.getId());
		container.validate();
		container.repaint();
	}

	public void search(String keyWord) {
		container.removeAll();
		System.out.println(tiles.values().size());
		Collection<VirtueApplicationItem> vas = tiles.values();
		List<VirtueApplicationItem> list = vas.stream()
				.filter(va -> va.getApplicationName().toLowerCase().contains(keyWord.toLowerCase()))
				.collect(Collectors.toList());

		for (VirtueApplicationItem va : list) {
			container.add(va.getContainer());
		}

		container.validate();
		container.repaint();
	}

	public JPanel getContainer() {
		return container;
	}

}
