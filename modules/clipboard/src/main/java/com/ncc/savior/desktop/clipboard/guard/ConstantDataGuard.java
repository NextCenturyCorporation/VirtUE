package com.ncc.savior.desktop.clipboard.guard;

import com.ncc.savior.virtueadmin.model.ClipboardPermissionOption;

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
	public ClipboardPermissionOption allowDataTransfer(String dataSourceGroupId, String dataDestinationGroupId) {
		return ClipboardPermissionOption.ALLOW;
	}

	@Override
	public void init() {
		// do nothing
	}

}
