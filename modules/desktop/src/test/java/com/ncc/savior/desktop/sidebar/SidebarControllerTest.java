package com.ncc.savior.desktop.sidebar;

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
	public void testDetectChangesAndReport() {
		ArrayList<String> changed = new ArrayList<String>();
		ArrayList<String> added = new ArrayList<String>();
		ArrayList<String> deleted = new ArrayList<String>();
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
		sc.setVirtueChangeHandler(vch);
		Map<String, ApplicationDefinition> apps = new HashMap<String, ApplicationDefinition>();
		DesktopVirtue v1 = new DesktopVirtue("1", "1", "1", apps, VirtueState.UNPROVISIONED);
		DesktopVirtue v2 = new DesktopVirtue("2", "2", "2", apps, VirtueState.UNPROVISIONED);
		DesktopVirtue v3 = new DesktopVirtue("3", "3", "3", apps, VirtueState.UNPROVISIONED);
		DesktopVirtue v3_2 = new DesktopVirtue("3", "3-2", "3", apps, VirtueState.UNPROVISIONED);
		DesktopVirtue v4 = new DesktopVirtue("4", "4", "4", apps, VirtueState.UNPROVISIONED);
		DesktopVirtue v5 = new DesktopVirtue("5", "5", "5", apps, VirtueState.UNPROVISIONED);
		DesktopVirtue v7 = new DesktopVirtue("7", "7", "7", apps, VirtueState.UNPROVISIONED);
		DesktopVirtue v7_2 = new DesktopVirtue("7", "7-2", "7", apps, VirtueState.UNPROVISIONED);
		DesktopVirtue v9 = new DesktopVirtue("9", "9", "9", apps, VirtueState.UNPROVISIONED);
		DesktopVirtue v10 = new DesktopVirtue("10", "10", "10", apps, VirtueState.UNPROVISIONED);
		List<DesktopVirtue> cv = new ArrayList<DesktopVirtue>();
		List<DesktopVirtue> nv = new ArrayList<DesktopVirtue>();
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
		sc.detectChangesAndReport(cv, nv);

		Assert.assertArrayEquals(new String[] { "10", "4" }, added.toArray());
		Assert.assertArrayEquals(new String[] { "3", "7" }, changed.toArray());
		Assert.assertArrayEquals(new String[] { "1", "2" }, deleted.toArray());
	}

}
