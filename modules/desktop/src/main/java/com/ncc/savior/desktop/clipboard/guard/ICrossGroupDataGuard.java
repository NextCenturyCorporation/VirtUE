package com.ncc.savior.desktop.clipboard.guard;

/**
 * Determines whether data can flow between groupIds (VirtueId's in our specific
 * scenario).
 *
 *
 */
public interface ICrossGroupDataGuard {

	boolean allowDataTransfer(String dataSourceGroupId, String dataDestinationGroupId);

}
