package com.ncc.savior.desktop.sidebar;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.swing.JPanel;

import com.ncc.savior.virtueadmin.model.desktop.DesktopVirtue;

/**
 * This is the virtue tile component that can be set as the view to the sidebar
 * scrollPane. It contains multiple virtueContainers
 *
 */

public class VirtueTile {
	private JPanel container;
	private ConcurrentHashMap<String, VirtueContainer> virtues;

	private static int row = 0;

	public VirtueTile() throws IOException {
		this.container = new JPanel();
		this.virtues = new ConcurrentHashMap<String, VirtueContainer>();
		GridBagLayout gbl = new GridBagLayout();
		gbl.columnWidths = new int[] { 455, 0 };
		gbl.rowHeights = new int[] { 100, 100, 0 };
		gbl.columnWeights = new double[] { 0.0, Double.MIN_VALUE };
		gbl.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		container.setLayout(gbl);
		VirtueContainer.resetRows();
	}

	public void removeVirtue(DesktopVirtue virtue) {
		VirtueContainer removedVc = virtues.get(virtue.getTemplateId());

		int removedRow = removedVc.getRow();
		container.remove(removedVc.getContainer());
		virtues.remove(virtue.getTemplateId());

		for (VirtueContainer vc : virtues.values()) {
			if (vc.getRow() > removedRow) {
				container.remove(vc.getContainer());
				vc.setRow(vc.getRow() - 1);
				addVirtueToRow(vc.getVirtue(), vc, vc.getRow());
				container.validate();
				container.repaint();
			}
		}
	}

	// Basically a reset function. Renders all the virtues and their tiles
	// alphabetically
	public void renderSorted(Comparator<VirtueContainer> comp) {
		row = 0;
		container.removeAll();
		Collection<VirtueContainer> vcs = virtues.values();
		ArrayList<VirtueContainer> vcList = new ArrayList<VirtueContainer>();
		vcList.addAll(vcs);

		if (comp != null) {
			Collections.sort(vcList, comp);
		} else {
			Collections.sort(vcList);
		}

		for (VirtueContainer vc : vcList) {
			addVirtueToRow(vc.getVirtue(), vc, row);
			row++;
		}

		for (VirtueContainer vc : virtues.values()) {
			vc.renderSorted();
		}

		container.validate();
		container.repaint();
	}

	public void search(String keyword) {
		row = 0;
		container.removeAll();
		Collection<VirtueContainer> vcs = virtues.values();
		List<VirtueContainer> matchedVcs = vcs.stream()
				.filter(vc -> vc.containsKeyword(keyword))
				.collect(Collectors.toList());
		Collections.sort(matchedVcs);

		for (VirtueContainer vc : matchedVcs) {
			vc.search(keyword);
			addVirtueToRow(vc.getVirtue(), vc, row);
			row++;
			container.validate();
			container.repaint();
		}
	}

	public void addVirtueToRow(DesktopVirtue virtue, VirtueContainer vc, int row) {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(0, 0, 10, 0);
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = row;
		container.add(vc.getContainer(), gbc);

		virtues.put(virtue.getTemplateId(), vc);
	}

	public JPanel getContainer() {
		return container;
	}

}
