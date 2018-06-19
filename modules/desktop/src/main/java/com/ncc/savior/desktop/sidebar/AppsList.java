package com.ncc.savior.desktop.sidebar;

import java.io.IOException;

import com.ncc.savior.desktop.virtues.VirtueService;

/**
 * This is the application list component that can be set as the view to the
 * sidebar scrollPane
 */

public class AppsList extends AbstractAppsView {

	public AppsList(VirtueService virtueService) throws IOException {
		super(virtueService);
		container.setLayout(new ModifiedFlowLayout());
	}

}
