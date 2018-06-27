package com.ncc.savior.desktop.sidebar;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.ncc.savior.desktop.virtues.VirtueService;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.desktop.DesktopVirtue;

/**
 * This is the application list component that can be set as the view to the
 * sidebar scrollPane
 */

public class AppsList extends AbstractAppsView {

	private JPanel footer = new JPanel();
	private int row = 0;

	public AppsList(VirtueService virtueService, JScrollPane sp) throws IOException {
		super(virtueService, sp);
		GridBagLayout gbl = new GridBagLayout();
		gbl.columnWidths = new int[] { 455, 0 };
		gbl.columnWeights = new double[] { 0.0, Double.MIN_VALUE };
		gbl.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		container.setLayout(gbl);
	}

	@Override
	public void addApplication(ApplicationDefinition ad, VirtueApplicationItem va) {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(0, 0, 0, 0);
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.PAGE_START;
		gbc.gridx = 0;
		gbc.gridy = row;
		container.remove(footer);
		container.add(va.getContainer(), gbc);
		container.validate();
		container.repaint();
		moveFooter(row + 1);
		row++;

		tiles.put(ad.getId() + va.getVirtue().getTemplateId(), va);
	}

	public void moveFooter(int row) {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(0, 0, 0, 0);
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = 0;
		gbc.gridy = row;
		container.add(footer, gbc);
	}

	public void removeVirtue(DesktopVirtue virtue) {
		for (ApplicationDefinition ad : virtue.getApps().values()) {
			tiles.remove(ad.getId() + virtue.getTemplateId());
		}
		search(null, null, null);
	}

	@Override
	public void addTile(VirtueApplicationItem va) {
		addApplication(va.getApplication(), va);
	}

}
