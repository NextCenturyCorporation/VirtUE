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
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

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
	protected JScrollPane sp;

	protected ArrayList<String> appsInView;

	public AbstractAppsView(VirtueService virtueService, JScrollPane sp) {
		this.virtueService = virtueService;
		this.sp = sp;
		this.container = new JPanel();
		this.tiles = new HashMap<String, VirtueApplicationItem>();
		this.appsInView = new ArrayList<String>();
	}

	public void addApplication(ApplicationDefinition ad, VirtueApplicationItem va) throws IOException {
		tiles.put(ad.getId() + va.getVirtue().getTemplateId(), va);
		appsInView.add(ad.getId() + va.getVirtue().getTemplateId());
		container.add(va.getContainer());

		container.validate();
		container.repaint();
	}

	public void removeApplication(ApplicationDefinition ad, DesktopVirtue virtue) {
		if (appsInView.contains(ad.getId() + virtue.getTemplateId())) {
			container.remove(tiles.get(ad.getId() + virtue.getTemplateId()).getContainer());
			container.validate();
			container.repaint();
		}
		tiles.remove(ad.getId() + virtue.getTemplateId());
		container.validate();
		container.repaint();
	}

	public void renderSorted(Comparator<VirtueApplicationItem> comp) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				container.removeAll();
				appsInView.clear();
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
				sp.setViewportView(sp.getViewport().getView());
			}
		});
	}

	public void search(String keyword) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				container.removeAll();
				appsInView.clear();
				Collection<VirtueApplicationItem> vas = tiles.values();
				List<VirtueApplicationItem> matchedVas = vas.stream()
						.filter(va -> va.getApplicationName().toLowerCase().contains(keyword.toLowerCase()))
						.collect(Collectors.toList());
				Collections.sort(matchedVas);

				for (VirtueApplicationItem va : matchedVas) {
					container.add(va.getContainer());
				}

				container.validate();
				container.repaint();
				sp.setViewportView(sp.getViewport().getView());
			}
		});
	}

	public JPanel getContainer() {
		return container;
	}

}
