package com.ncc.savior.desktop.sidebar;

import javax.swing.JComponent;
import javax.swing.MenuSelectionManager;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicCheckBoxMenuItemUI;

public class StayOpenCheckBoxMenuItem extends BasicCheckBoxMenuItemUI {

	@Override
	protected void doClick(MenuSelectionManager msm) {
		menuItem.doClick(0);
	}

	public static ComponentUI createUI(JComponent c) {
		return new StayOpenCheckBoxMenuItem();
	}
}