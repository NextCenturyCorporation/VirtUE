package com.ncc.savior.desktop.clipboard.guard;

public class ConstantDataGuard implements ICrossGroupDataGuard {

	private boolean allow;

	public ConstantDataGuard(boolean allow) {
		this.allow = allow;
	}

	@Override
	public boolean allowDataTransfer(String dataSourceGroupId, String dataDestinationGroupId) {
		return allow;
	}

}
