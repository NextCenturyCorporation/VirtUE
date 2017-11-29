package com.ncc.savior.desktop.sidebar;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.ncc.savior.desktop.sidebar.SidebarController.VirtueChangeHandler;
import com.ncc.savior.desktop.virtues.VirtueAppDto;
import com.ncc.savior.desktop.virtues.VirtueDto;

public class SidebarControllerTest {

	@Test
	public void testDetectChangesAndReport() {
		ArrayList<String> changed = new ArrayList<String>();
		ArrayList<String> added = new ArrayList<String>();
		ArrayList<String> deleted = new ArrayList<String>();
		VirtueChangeHandler vch = new VirtueChangeHandler() {

			@Override
			public void removeVirtue(VirtueDto virtue) {
				deleted.add(virtue.getId());
			}

			@Override
			public void changeVirtue(VirtueDto virtue) {
				changed.add(virtue.getId());
			}

			@Override
			public void addVirtue(VirtueDto virtue) {
				added.add(virtue.getId());
			}
		};
		SidebarController sc = new SidebarController(null, null);
		sc.setVirtueChangeHandler(vch);
		List<VirtueAppDto> apps = new ArrayList<VirtueAppDto>();
		VirtueDto v1 = new VirtueDto("1", "1", "1", apps);
		VirtueDto v2 = new VirtueDto("2", "2", "2", apps);
		VirtueDto v3 = new VirtueDto("3", "3", "3", apps);
		VirtueDto v3_2 = new VirtueDto("3", "3-2", "3", apps);
		VirtueDto v4 = new VirtueDto("4", "4", "4", apps);
		VirtueDto v5 = new VirtueDto("5", "5", "5", apps);
		VirtueDto v7 = new VirtueDto("7", "7", "7", apps);
		VirtueDto v7_2 = new VirtueDto("7", "7-2", "7", apps);
		VirtueDto v9 = new VirtueDto("9", "9", "9", apps);
		VirtueDto v10 = new VirtueDto("10", "10", "10", apps);
		List<VirtueDto> cv = new ArrayList<VirtueDto>();
		List<VirtueDto> nv = new ArrayList<VirtueDto>();
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
