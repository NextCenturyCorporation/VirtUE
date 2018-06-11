package com.ncc.savior.desktop.sidebar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.ncc.savior.desktop.sidebar.SidebarController.VirtueChangeHandler;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.VirtueState;
import com.ncc.savior.virtueadmin.model.desktop.DesktopVirtue;

public class SidebarControllerTest {

	@Test
	public void testDetectChangesAndReport() throws IOException {
		ArrayList<String> changed = new ArrayList<String>();
		ArrayList<String> added = new ArrayList<String>();
		ArrayList<String> deleted = new ArrayList<String>();
		VirtueChangeHandler doNothing = new VirtueChangeHandler() {
			@Override
			public void removeVirtue(DesktopVirtue virtue) {
			}

			@Override
			public void changeVirtue(DesktopVirtue virtue) {
			}

			@Override
			public void addVirtue(DesktopVirtue virtue) {
			}
		};
		VirtueChangeHandler vch = new VirtueChangeHandler() {

			@Override
			public void removeVirtue(DesktopVirtue virtue) {
				deleted.add(virtue.getId());
			}

			@Override
			public void changeVirtue(DesktopVirtue virtue) {
				changed.add(virtue.getId());
			}

			@Override
			public void addVirtue(DesktopVirtue virtue) {
				added.add(virtue.getId());
			}
		};
		SidebarController sc = new SidebarController(null, null, null);

		Map<String, ApplicationDefinition> apps = new HashMap<String, ApplicationDefinition>();
		DesktopVirtue v1 = new DesktopVirtue("1", "1", "1", apps, VirtueState.RUNNING);
		DesktopVirtue v2 = new DesktopVirtue("2", "2", "2", apps, VirtueState.RUNNING);
		DesktopVirtue v3 = new DesktopVirtue("3", "3", "3", apps, VirtueState.RUNNING);
		DesktopVirtue v3_2 = new DesktopVirtue("3", "3-2", "3", apps, VirtueState.RUNNING);
		DesktopVirtue v4 = new DesktopVirtue("4", "4", "4", apps, VirtueState.RUNNING);
		DesktopVirtue v5 = new DesktopVirtue("5", "5", "5", apps, VirtueState.RUNNING);
		DesktopVirtue v7 = new DesktopVirtue("7", "7", "7", apps, VirtueState.RUNNING);
		DesktopVirtue v7_2 = new DesktopVirtue("7", "7-2", "7", apps, VirtueState.RUNNING);
		DesktopVirtue v9 = new DesktopVirtue("9", "9", "9", apps, VirtueState.RUNNING);
		DesktopVirtue v10 = new DesktopVirtue("10", "10", "10", apps, VirtueState.RUNNING);
		List<DesktopVirtue> nv = new ArrayList<DesktopVirtue>();
		List<DesktopVirtue> cv = new ArrayList<DesktopVirtue>();
		nv.add(v3_2);
		nv.add(v7_2);
		nv.add(v5);
		nv.add(v10);
		nv.add(v4);
		nv.add(v9);

		cv.add(v3);
		cv.add(v7);
		cv.add(v1);
		cv.add(v2);
		cv.add(v5);
		cv.add(v9);
		sc.setVirtueChangeHandler(doNothing);
		sc.updateVirtues(cv);
		sc.setVirtueChangeHandler(vch);
		sc.updateVirtues(nv);

		Assert.assertArrayEquals(new String[] { "10", "4" }, added.toArray());
		Assert.assertArrayEquals(new String[] { "3", "7", "5", "9" }, changed.toArray());
		Assert.assertArrayEquals(new String[] { "1", "2" }, deleted.toArray());
	}

	// private void add(Map<String, DesktopVirtue> cv, DesktopVirtue v) {
	// if (v.getId() != null) {
	// cv.put(v.getTemplateId() + "-" + v.getId(), v);
	// } else {
	// cv.put(v.getTemplateId(), v);
	// }
	//
	// }

}
