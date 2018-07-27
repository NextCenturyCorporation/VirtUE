package com.ncc.savior.virtueadmin.service;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private static final Logger logger = LoggerFactory.getLogger(PermissionService.class);
	private IPermissionDao permissionDao;
	private ClipboardPermissionOption defaultClipboardPermission;

	public PermissionService(IPermissionDao permissionDao) {
		this.permissionDao = permissionDao;
		this.defaultClipboardPermission = ClipboardPermissionOption.DENY;
	}

	public void test() {
		logger.debug("all: " + permissionDao.getAllClipboardPermissions());
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

	public Map<String, ClipboardPermission> getClipboardPermissionForSource(String sourceId) {
		permissionDao.getClipboardPermissionForSource(sourceId);
		return null;
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

	public Iterable<ClipboardPermission> getAllRawPermissions() {
		return permissionDao.getAllClipboardPermissions();
	}

	private Map<Pair<String, String>, ClipboardPermissionOption> getAllRawPermissionsAsMap() {
		Iterable<ClipboardPermission> raw = getAllRawPermissions();
		Map<Pair<String, String>, ClipboardPermissionOption> map = new TreeMap<Pair<String, String>, ClipboardPermissionOption>();
		for (ClipboardPermission perm : raw) {
			ImmutablePair<String, String> pair = new ImmutablePair<String, String>(perm.getSourceGroupId(),
					perm.getDestinationGroupId());
			map.put(pair, perm.getPermission());
		}
		return map;
	}

	public Map<Pair<String, String>, ClipboardPermissionOption> getAllPermissionsForSources(
			Collection<String> sourceIds) {
		// Map<Pair<String, String>, ClipboardPermissionOption> map = new
		// TreeMap<Pair<String, String>, ClipboardPermissionOption>();
		Map<Pair<String, String>, ClipboardPermissionOption> rawMap = getAllRawPermissionsAsMap();
		for (String sourceId : sourceIds) {
			for (String destinationId : sourceIds) {
				ImmutablePair<String, String> pair = new ImmutablePair<String, String>(sourceId, destinationId);
				if (!rawMap.containsKey(pair)) {
					ClipboardPermissionOption permission = getPermissionWithDefaults(pair, rawMap);
					rawMap.put(pair, permission);
				}
			}
		}
		return rawMap;
	}

	/**
	 * Get a permission first by looking in the map. If that doesn't exist, look for
	 * the source default by changing the destination in the key to the default id
	 * and check the map again. If that doesn't work, then use the default for the
	 * service.
	 * 
	 * @param pair
	 * @param rawMap
	 * @return
	 */
	private ClipboardPermissionOption getPermissionWithDefaults(ImmutablePair<String, String> pair,
			Map<Pair<String, String>, ClipboardPermissionOption> rawMap) {
		ClipboardPermissionOption cpo = rawMap.get(pair);
		if (cpo == null) {
			ImmutablePair<String, String> sourceDefaultPair = new ImmutablePair<String, String>(pair.left,
					ClipboardPermission.DEFAULT_DESTINATION);
			cpo = rawMap.get(sourceDefaultPair);
		}
		if (cpo == null) {
			cpo = defaultClipboardPermission;
		}
		return cpo;
	}

	public void setClipboardPermission(String sourceId, String destinationId, ClipboardPermissionOption option) {
		permissionDao.setClipboardPermission(sourceId, destinationId, option);
	}

	public ClipboardPermissionOption getDefaultClipboardPermission() {
		return defaultClipboardPermission;
	}

	public void setDefaultClipboardPermission(ClipboardPermissionOption defaultClipboardPermission) {
		this.defaultClipboardPermission = defaultClipboardPermission;
	}

}
