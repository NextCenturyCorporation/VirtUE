package com.ncc.savior.desktop.sidebar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JPanel;

import com.ncc.savior.desktop.virtues.VirtueService;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.desktop.DesktopVirtue;

/**
 *
 * This is a superclass for appsList, appsTile, and favoritesView and it
 * includes the basic common components of each different view
 *
 */

public abstract class AbstractAppsView {

	protected VirtueService virtueService;
	protected JPanel container;
	protected HashMap<String, VirtueApplicationItem> tiles;

	public AbstractAppsView(VirtueService virtueService) {
		this.virtueService = virtueService;
		this.container = new JPanel();
		this.tiles = new HashMap<String, VirtueApplicationItem>();
	}

	public void addApplication(ApplicationDefinition ad, VirtueApplicationItem va) throws IOException {
		tiles.put(ad.getId() + va.getVirtue().getTemplateId(), va);
		container.add(va.getContainer());

		container.validate();
		container.repaint();
	}

	public void removeApplication(ApplicationDefinition ad, DesktopVirtue virtue) {
		container.remove(tiles.get(ad.getId() + virtue.getTemplateId()).getContainer());
		container.validate();
		container.repaint();
		tiles.remove(ad.getId() + virtue.getTemplateId());
		container.validate();
		container.repaint();
	}

	public void renderSorted(Comparator<VirtueApplicationItem> comp) {
		container.removeAll();
		Collection<VirtueApplicationItem> vas = tiles.values();
		ArrayList<VirtueApplicationItem> vaList = new ArrayList<VirtueApplicationItem>();
		vaList.addAll(vas);
		if (comp != null) {
			Collections.sort(vaList, comp);
		} else {
			Collections.sort(vaList);
		}
		for (VirtueApplicationItem va : vaList) {
			container.add(va.getContainer());
		}
		container.validate();
		container.repaint();
	}

	public void search(String keyword) {
		container.removeAll();
		Collection<VirtueApplicationItem> vas = tiles.values();
		List<VirtueApplicationItem> matchedVas = vas.stream()
				.filter(va -> va.getApplicationName().toLowerCase().contains(keyword.toLowerCase()))
				.collect(Collectors.toList());

		for (VirtueApplicationItem va : matchedVas) {
			container.add(va.getContainer());
		}

		container.validate();
		container.repaint();
	}

	public JPanel getContainer() {
		return container;
	}

}
