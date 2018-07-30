package com.ncc.savior.desktop.clipboard.guard;

import com.ncc.savior.virtueadmin.model.ClipboardPermissionOption;

/**
 * Determines whether data can flow between groupIds (VirtueId's in our specific
 * scenario). Group IDs are just a generic ID set for whatever group of machines
 * the user needs. For each combination of ID, a data guard can set a different
 * protection mechanism between them.
 *
 *
 */
public interface ICrossGroupDataGuard {

	/**
	 * Returns true if data should be allowed to be transfered from
	 * dataSourceGroupId and dataDestinationGroupId.
	 *
	 * @param dataSourceGroupId
	 * @param dataDestinationGroupId
	 * @return
	 */
	ClipboardPermissionOption allowDataTransfer(String dataSourceGroupId, String dataDestinationGroupId);

	/**
	 * do any initialization that the data guard requires after the system has been
	 * setup. This could be tasks such as initializing caches.
	 */
	public void init();

}
