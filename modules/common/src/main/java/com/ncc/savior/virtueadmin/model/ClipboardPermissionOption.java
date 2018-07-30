package com.ncc.savior.virtueadmin.model;

/**
 * Options for the clipboard permissions.
 * <ul>
 * <li>ALLOW - Allow all data to be transfered
 * <li>DENY - Block all data from being transfered
 * <li>ASK - Prompt the user for whether they want the specified data to be
 * transfered
 * </ul>
 */
public enum ClipboardPermissionOption {
	ALLOW, DENY, ASK
}
