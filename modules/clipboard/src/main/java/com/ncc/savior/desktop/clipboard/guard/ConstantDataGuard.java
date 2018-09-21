package com.ncc.savior.desktop.clipboard.guard;

import java.util.HashMap;

/**
 * Implementation of {@link ICrossGroupDataGuard} which gives a static response.
 * The response is based on the value given to the constructor.
 *
 */
public class ConstantDataGuard implements ICrossGroupDataGuard {

	private boolean allow;

	public ConstantDataGuard(boolean allow) {
		this.allow = allow;
	}

	@Override
	public boolean allowDataTransfer(String dataSourceGroupId, String dataDestinationGroupId) {
		return allow;
	}

	@Override
	public void init() {
		// do nothing
	}

	@Override
	public void setGroupIdToDisplayNameMap(HashMap<String, String> groupIdToDisplayName) {
		// this implementation never displays anything, so we don't need to store a
		// reference to the map.
	}

}
