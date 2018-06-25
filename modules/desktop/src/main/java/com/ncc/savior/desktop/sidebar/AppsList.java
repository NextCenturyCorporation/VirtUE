package com.ncc.savior.desktop.sidebar;

import java.io.IOException;

import javax.swing.JScrollPane;

import com.ncc.savior.desktop.virtues.VirtueService;

/**
 * This is the application list component that can be set as the view to the
 * sidebar scrollPane
 */

public class AppsList extends AbstractAppsView {

	public AppsList(VirtueService virtueService, JScrollPane sp) throws IOException {
		super(virtueService, sp);
		container.setLayout(new ModifiedFlowLayout());
	}

}
