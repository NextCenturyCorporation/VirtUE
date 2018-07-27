package com.ncc.savior.virtueadmin.data.jpa;

import java.util.List;

import com.ncc.savior.virtueadmin.model.ClipboardPermission;
import com.ncc.savior.virtueadmin.model.ClipboardPermissionOption;

/**
 * Data Access Object interface that handles setting and retrieving permissions
 * for different services. The first service will be the clipboard dataguard.
 * 
 *
 */
public interface IPermissionDao {

	/**
	 * Retrieves the permission stored (if any) for a given source and destination.
	 * Will return null if no permission found.
	 * 
	 * @param sourceGroupId
	 * @param destinationGroupId
	 * @return
	 */
	ClipboardPermission getClipboardPermission(String sourceGroupId, String destinationGroupId);

	void setClipboardPermission(String sourceId, String destinationId, ClipboardPermissionOption option);

	List<ClipboardPermission> getClipboardPermissionForSource(String sourceId);

	List<ClipboardPermission> getClipboardPermissionForDestination(String destinationId);

	Iterable<ClipboardPermission> getAllClipboardPermissions();

}