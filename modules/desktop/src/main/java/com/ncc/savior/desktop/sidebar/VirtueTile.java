package com.ncc.savior.desktop.sidebar;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.desktop.DesktopVirtue;

/**
 * This is the virtue tile component that can be set as the view to the sidebar
 * scrollPane. It contains multiple virtueContainers
 *
 */

public class VirtueTile extends AbstractVirtueView {

	private ConcurrentHashMap<String, VirtueTileContainer> virtues;

	public VirtueTile(JScrollPane sp) throws IOException {
		super(sp);
		this.virtues = new ConcurrentHashMap<String, VirtueTileContainer>();
	}

	public void removeVirtue(DesktopVirtue virtue) {
		virtues.remove(virtue.getTemplateId());

		triggerRemoveVirtueListener();
	}

	public void updateApp(ApplicationDefinition ad, DesktopVirtue virtue) {
		if (virtue != null) {
			VirtueTileContainer vtc = virtues.get(virtue.getTemplateId());
			if (vtc != null) {
				VirtueApplicationItem va = vtc.tiles.get(ad.getId() + virtue.getTemplateId());
				if (va != null) {
					va.update(virtue);
				}
			}
		}
	}

	public void search(String keyword, Comparator<VirtueTileContainer> vcComp, Comparator<VirtueApplicationItem> vaiComp) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				row = 0;
				container.removeAll();
				Collection<VirtueTileContainer> vcs = virtues.values();
				List<VirtueTileContainer> matchedVcs;

				if (keyword != null) {
					matchedVcs = vcs.stream().filter(vc -> vc.containsKeyword(keyword)).collect(Collectors.toList());
				} else {
					matchedVcs = vcs.stream().collect(Collectors.toList());
				}

				if (vcComp != null) {
					Collections.sort(matchedVcs, vcComp);
				} else {
					Collections.sort(matchedVcs);
				}

				for (VirtueTileContainer vc : matchedVcs) {
					if (keyword != null) {
						if (vaiComp != null) {
							vc.search(vaiComp,
									va -> va.getApplicationName().toLowerCase().contains(keyword.toLowerCase()));
						} else {
							vc.search(null,
									va -> va.getApplicationName().toLowerCase().contains(keyword.toLowerCase()));
						}
					} else {
						vc.search(null, null);
					}
					vc.setRow(row);
					addVirtueToRow(vc.getVirtue(), vc, row);
					row++;
					container.validate();
					container.repaint();
				}
				sp.setViewportView(sp.getViewport().getView());
			}
		});
	}

	public void addVirtueToRow(DesktopVirtue virtue, VirtueTileContainer vc, int row) {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(0, 0, 10, 0);
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = row;
		gbc.anchor = GridBagConstraints.PAGE_START;
		container.remove(footer);
		container.add(vc.getContainer(), gbc);
		moveFooter(row + 1);

		virtues.put(virtue.getTemplateId(), vc);
	}

}
