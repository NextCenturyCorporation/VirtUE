package com.ncc.savior.desktop.sidebar;

import java.awt.FlowLayout;
import java.io.IOException;

import javax.swing.BorderFactory;

import com.ncc.savior.desktop.virtues.VirtueService;

/**
 * This is the application tile component that can be set as the view to the
 * sidebar scrollPane
 */

public class AppsTile extends AbstractAppsView {

	public AppsTile(VirtueService virtueService)
			throws IOException {
		super(virtueService);
		container.setLayout(new ModifiedFlowLayout(FlowLayout.CENTER, 20, 20));

		container.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		container.validate();
		container.repaint();
	}

	// @SuppressWarnings("unchecked")
	// public void sort() {
	// container.removeAll();
	// Collection<VirtueApplicationItem> vas = tiles.values();
	// ArrayList<VirtueApplicationItem> vaList = new
	// ArrayList<VirtueApplicationItem>();
	// vaList.addAll(vas);
	// Collections.sort(vaList);
	// for (VirtueApplicationItem va : vaList) {
	// container.add(va.getContainer());
	// }
	// container.validate();
	// container.repaint();
	// }

}
