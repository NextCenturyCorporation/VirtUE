package com.ncc.savior.desktop.sidebar;

import java.awt.FlowLayout;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JScrollPane;

import com.ncc.savior.desktop.virtues.VirtueService;

/**
 * This is the application tile component that can be set as the view to the
 * sidebar scrollPane
 */

public class AppsTile extends AbstractAppsView {

	public AppsTile(VirtueService virtueService, JScrollPane sp)
			throws IOException {
		super(virtueService, sp);
		container.setLayout(new ModifiedFlowLayout(FlowLayout.CENTER, 20, 20));

		container.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		container.validate();
		container.repaint();
	}

}
