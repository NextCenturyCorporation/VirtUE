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

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import com.ncc.savior.virtueadmin.model.desktop.DesktopVirtue;

/**
 *
 * This class should be correctly implemented at some point to provide a second
 * view for virtues
 *
 */

public class VirtueList extends AbstractVirtueView {

	private ConcurrentHashMap<String, VirtueListContainer> virtues;

	public VirtueList(JScrollPane sp) throws IOException {
		super(sp);
		this.virtues = new ConcurrentHashMap<String, VirtueListContainer>();
	}

	public void removeVirtue(DesktopVirtue virtue) {
		row--;
		container.remove(footer);
		moveFooter(row);

		VirtueListContainer removedVc = virtues.get(virtue.getTemplateId());

		int removedRow = removedVc.getRow();
		virtues.remove(virtue.getTemplateId());

		if (virtuesInView.contains(virtue.getTemplateId())) {
			container.remove(removedVc.getContainer());

			for (VirtueListContainer vc : virtues.values()) {
				if (vc.getRow() > removedRow) {
					container.remove(vc.getContainer());
					vc.setRow(vc.getRow() - 1);
					addVirtueToRow(vc.getVirtue(), vc, vc.getRow());
					container.validate();
					container.repaint();
				}
			}
		}
	}

	public void addVirtueToRow(DesktopVirtue virtue, VirtueListContainer vlc, int row) {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(0, 0, 0, 0);
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = row;
		gbc.anchor = GridBagConstraints.PAGE_START;
		container.remove(footer);
		container.add(vlc.getContainer(), gbc);
		moveFooter(row + 1);
		virtuesInView.add(virtue.getTemplateId());

		virtues.put(virtue.getTemplateId(), vlc);
	}

	public void search(String keyword, Comparator<VirtueListContainer> vlcComp,
			Comparator<VirtueApplicationItem> vaiComp) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				row = 0;
				container.removeAll();
				virtuesInView.clear();
				Collection<VirtueListContainer> vlcs = virtues.values();
				List<VirtueListContainer> matchedVlcs;

				if (keyword != null) {
					matchedVlcs = vlcs.stream().filter(vlc -> vlc.containsKeyword(keyword))
							.collect(Collectors.toList());
				} else {
					matchedVlcs = vlcs.stream().collect(Collectors.toList());
				}

				if (vlcComp != null) {
					Collections.sort(matchedVlcs, vlcComp);
				} else {
					Collections.sort(matchedVlcs);
				}

				for (VirtueListContainer vlc : matchedVlcs) {
					if (keyword != null) {
						if (vaiComp != null) {
							vlc.search(vaiComp,
									va -> va.getApplicationName().toLowerCase().contains(keyword.toLowerCase()));
						} else {
							vlc.search(null,
									va -> va.getApplicationName().toLowerCase().contains(keyword.toLowerCase()));
						}
					} else {
						vlc.search(null, null);
					}
					vlc.setRow(row);
					addVirtueToRow(vlc.getVirtue(), vlc, row);
					row++;
					container.validate();
					container.repaint();
				}
				sp.setViewportView(sp.getViewport().getView());
			}
		});
	}

	public JScrollPane getScroll() {
		return sp;
	}

	@Override
	public JPanel getContainer() {
		return container;
	}
}
