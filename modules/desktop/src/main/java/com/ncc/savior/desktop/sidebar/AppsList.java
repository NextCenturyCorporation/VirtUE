package com.ncc.savior.desktop.sidebar;

import java.io.IOException;

import javax.swing.JScrollPane;

import com.ncc.savior.desktop.virtues.VirtueService;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;

/**
 * This is the application list component that can be set as the view to the
 * sidebar scrollPane
 */

public class AppsList extends AbstractAppsView {

	public AppsList(VirtueService vs, JScrollPane sp) throws IOException {
		super(vs, sp);
		container.setLayout(new ModifiedFlowLayout());
	}

	public void addApplication(ApplicationDefinition ad, VirtueApplicationItem va) throws IOException {
		tiles.put(ad, va.getListContainer());
		container.add(va.getListContainer());
		container.validate();
		container.repaint();
	}

}
