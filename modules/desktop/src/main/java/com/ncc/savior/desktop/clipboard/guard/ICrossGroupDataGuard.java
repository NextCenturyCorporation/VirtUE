package com.ncc.savior.desktop.clipboard.guard;

/**
 * Determines whether data can flow between groupIds (VirtueId's in our specific
 * scenario).
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
	boolean allowDataTransfer(String dataSourceGroupId, String dataDestinationGroupId);

}
