package com.ncc.savior.virtueadmin.model;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Options for the clipboard permissions.
 * <ul>
 * <li>ALLOW - Allow all data to be transfered
 * <li>DENY - Block all data from being transfered
 * <li>ASK - Prompt the user for whether they want the specified data to be
 * transfered
 * </ul>
 */
@Schema(description="The option for a clipboard message permission.")
public enum ClipboardPermissionOption {
	ALLOW, DENY, ASK
}
