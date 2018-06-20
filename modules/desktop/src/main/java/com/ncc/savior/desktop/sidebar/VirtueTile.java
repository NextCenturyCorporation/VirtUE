package com.ncc.savior.desktop.sidebar;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import java.util.HashMap;

import javax.swing.JPanel;

import com.ncc.savior.virtueadmin.model.desktop.DesktopVirtue;

/**
 * This is the virtue tile component that can be set as the view to the sidebar
 * scrollPane. It contains multiple virtueContainers
 *
 */

public class VirtueTile {
	private JPanel container;
	private HashMap<String, VirtueContainer> virtues;

	public VirtueTile() throws IOException {
		this.container = new JPanel();
		this.virtues = new HashMap<String, VirtueContainer>();
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
