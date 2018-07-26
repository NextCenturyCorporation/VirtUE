package com.ncc.savior.virtueadmin.service;

import com.ncc.savior.virtueadmin.data.jpa.IPermissionDao;
import com.ncc.savior.virtueadmin.model.ClipboardPermission;
import com.ncc.savior.virtueadmin.model.ClipboardPermissionOption;

/**
 * Service to create, retrieve, and handle permissions for services like the
 * clipboard data guard.
 * 
 *
 */
public class PermissionService {
	private IPermissionDao permissionDao;
	private ClipboardPermissionOption defaultClipboardPermission;

	public PermissionService(IPermissionDao permissionDao) {
		this.permissionDao = permissionDao;
		this.defaultClipboardPermission = ClipboardPermissionOption.DENY;
	}

	/**
	 * Returns the clipboard permission for a given source and destination. If none
	 * was set, it will first check the default for the source. IF none was found,
	 * it will use the default set by the service.
	 * 
	 * @param sourceId
	 * @param destinationId
	 * @return
	 */
	public ClipboardPermission getClipboardPermission(String sourceId, String destinationId) {
		ClipboardPermission permission = permissionDao.getClipboardPermission(sourceId, destinationId);
		if (permission == null && !ClipboardPermission.DEFAULT_DESTINATION.equals(destinationId)) {
			// TODO should this be at a higher level? Probalby
			// if we get nothing and weren't already looking for the default, get the
			// default;
			permission = permissionDao.getClipboardPermission(sourceId, ClipboardPermission.DEFAULT_DESTINATION);
		}
		if (permission == null) {
			return new ClipboardPermission(sourceId, destinationId, defaultClipboardPermission);
		}
		return permission;
	}

	/**
	 * Returns the permission stored in the database or null if none found. This
	 * method does not replace a null value with any defaults.
	 * 
	 * @param sourceId
	 * @param destinationId
	 * @return
	 */
	public ClipboardPermission getRawClipboardPermission(String sourceId, String destinationId) {
		ClipboardPermission permission = permissionDao.getClipboardPermission(sourceId, destinationId);
		return permission;
	}

	public void setClipboardPermission(String sourceId, String destinationId,
			ClipboardPermissionOption option) {
		permissionDao.setClipboardPermission(sourceId, destinationId, option);
	}

	public ClipboardPermissionOption getDefaultClipboardPermission() {
		return defaultClipboardPermission;
	}

	public void setDefaultClipboardPermission(ClipboardPermissionOption defaultClipboardPermission) {
		this.defaultClipboardPermission = defaultClipboardPermission;
	}

}
