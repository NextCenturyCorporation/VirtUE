package com.ncc.savior.desktop.sidebar;

import java.awt.FlowLayout;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JScrollPane;

import com.ncc.savior.desktop.virtues.VirtueService;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;

/**
 * This is the application tile component that can be set as the view to the
 * sidebar scrollPane
 */

public class AppsTile extends AbstractAppsView {

	public AppsTile(VirtueService vs, JScrollPane sp) throws IOException {
		super(vs, sp);
		container.setLayout(new ModifiedFlowLayout(FlowLayout.CENTER, 20, 20));

		container.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		container.validate();
		container.repaint();
	}

	public void addApplication(ApplicationDefinition ad, VirtueApplicationItem va) throws IOException {
		container.add(va.getTileContainer());
		tiles.put(ad, va.getTileContainer());
	}

}
