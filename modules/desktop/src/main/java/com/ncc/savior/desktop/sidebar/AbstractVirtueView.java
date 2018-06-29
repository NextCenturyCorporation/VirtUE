package com.ncc.savior.desktop.sidebar;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.desktop.DesktopVirtue;

public abstract class AbstractVirtueView {
	protected JPanel container;
	protected ArrayList<String> virtuesInView;

	protected static int row = 0;
	protected JPanel footer = new JPanel();

	protected ConcurrentHashMap<String, AbstractVirtueContainer> virtues;

	protected JScrollPane sp;

	public AbstractVirtueView(JScrollPane sp) {
		this.sp = sp;
		this.container = new JPanel();
		this.virtuesInView = new ArrayList<String>();

		GridBagLayout gbl = new GridBagLayout();
		gbl.columnWidths = new int[] { 455, 0 };
		gbl.columnWeights = new double[] { 0.0, Double.MIN_VALUE };
		gbl.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		container.setLayout(gbl);
	}

	public void moveFooter(int row) {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(0, 0, 0, 0);
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = 0;
		gbc.gridy = row;
		gbc.anchor = GridBagConstraints.PAGE_START;
		container.add(footer, gbc);
	}

	public void addVirtueToRow(DesktopVirtue virtue, VirtueTileContainer vc, int row, int padding) {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(0, 0, padding, 0);
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = row;
		gbc.anchor = GridBagConstraints.PAGE_START;
		container.remove(footer);
		container.add(vc.getContainer(), gbc);
		moveFooter(row + 1);
		virtuesInView.add(virtue.getTemplateId());

		virtues.put(virtue.getTemplateId(), vc);
	}

	public JPanel getContainer() {
		return container;
	}

	public void updateApp(ApplicationDefinition ad, DesktopVirtue virtue) {
		VirtueApplicationItem va = virtues.get(ad.getId()).tiles.get(ad.getId() + virtue.getTemplateId());
		if (va != null) {
			va.update(virtue);
		}
	}

}
