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
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import com.ncc.savior.virtueadmin.model.desktop.DesktopVirtue;

/**
 * This is the virtue tile component that can be set as the view to the sidebar
 * scrollPane. It contains multiple virtueContainers
 *
 */

public class VirtueTile {
	private JPanel container;
	private ConcurrentHashMap<String, VirtueContainer> virtues;
	private ArrayList<String> virtuesInView;

	private static int row = 0;

	private JScrollPane sp;

	public VirtueTile(JScrollPane sp) throws IOException {
		this.container = new JPanel();
		this.virtues = new ConcurrentHashMap<String, VirtueContainer>();
		this.virtuesInView = new ArrayList<String>();
		this.sp = sp;
		GridBagLayout gbl = new GridBagLayout();
		gbl.columnWidths = new int[] { 455, 0 };
		gbl.rowHeights = new int[] { 100, 100, 0 };
		gbl.columnWeights = new double[] { 0.0, Double.MIN_VALUE };
		gbl.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		container.setLayout(gbl);
		VirtueContainer.resetRows();
	}

	public void removeVirtue(DesktopVirtue virtue) {
		renderSorted(null);

		VirtueContainer removedVc = virtues.get(virtue.getTemplateId());

		int removedRow = removedVc.getRow();
		virtues.remove(virtue.getTemplateId());

		if (virtuesInView.contains(virtue.getTemplateId())) {
			container.remove(removedVc.getContainer());

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
	}

	// Basically a reset function. Renders all the virtues and their tiles
	// alphabetically by default
	public void renderSorted(Comparator<VirtueContainer> comp) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				row = 0;
				container.removeAll();
				virtuesInView.clear();
				Collection<VirtueContainer> vcs = virtues.values();
				ArrayList<VirtueContainer> vcList = new ArrayList<VirtueContainer>();
				vcList.addAll(vcs);

				if (comp != null) {
					Collections.sort(vcList, comp);
				} else {
					Collections.sort(vcList);
				}

				for (VirtueContainer vc : vcList) {
					vc.setRow(row);
					addVirtueToRow(vc.getVirtue(), vc, row);
					row++;
				}

				for (VirtueContainer vc : virtues.values()) {
					vc.renderSorted();
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
				row = 0;
				container.removeAll();
				virtuesInView.clear();
				Collection<VirtueContainer> vcs = virtues.values();
				List<VirtueContainer> matchedVcs = vcs.stream().filter(vc -> vc.containsKeyword(keyword))
						.collect(Collectors.toList());
				Collections.sort(matchedVcs);

				for (VirtueContainer vc : matchedVcs) {
					vc.search(keyword);
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

	public void addVirtueToRow(DesktopVirtue virtue, VirtueContainer vc, int row) {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(0, 0, 10, 0);
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = row;
		container.add(vc.getContainer(), gbc);
		virtuesInView.add(virtue.getTemplateId());

		virtues.put(virtue.getTemplateId(), vc);
	}

	public JPanel getContainer() {
		return container;
	}

}
