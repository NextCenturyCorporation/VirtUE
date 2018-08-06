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

	/**
	 * Set a single permission
	 * 
	 * @param sourceId
	 * @param destinationId
	 * @param option
	 */
	void setClipboardPermission(String sourceId, String destinationId, ClipboardPermissionOption option);

	/**
	 * Retrieve all permissions stored in the database with a given source ID.
	 * 
	 * @param sourceId
	 * @return
	 */
	List<ClipboardPermission> getClipboardPermissionForSource(String sourceId);

	/**
	 * Retrieve all permissions stored in the database with a given destination ID.
	 * 
	 * @param sourceId
	 * @return
	 */
	List<ClipboardPermission> getClipboardPermissionForDestination(String destinationId);

	/**
	 * Retrieve all permissions stored in the database.
	 * 
	 * @param sourceId
	 * @return
	 */
	Iterable<ClipboardPermission> getAllClipboardPermissions();

	/**
	 * Deletes a permission from the database
	 */
	void clearPermission(String sourceId, String destId);

}