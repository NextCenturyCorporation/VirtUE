package com.ncc.savior.desktop.sidebar.defaultapp;

import java.util.Comparator;

import com.ncc.savior.desktop.sidebar.VirtueListContainer;

public class VirtueStatusComparator implements Comparator<VirtueListContainer> {
	@Override
	public int compare(VirtueListContainer va1, VirtueListContainer va2) {

		Integer va1State = va1.getVirtue().getVirtueState().getValue();
		Integer va2State = va2.getVirtue().getVirtueState().getValue();

		int valComp = va1State.compareTo(va2State);

		if (valComp != 0) {
			return valComp;
		}

		return va1.getName().compareTo(va2.getName());
	}
}
