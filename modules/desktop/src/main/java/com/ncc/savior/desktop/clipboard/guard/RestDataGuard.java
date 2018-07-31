package com.ncc.savior.desktop.clipboard.guard;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JOptionPane;

import org.apache.commons.collections4.map.PassiveExpiringMap;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.virtues.DesktopResourceService;
import com.ncc.savior.virtueadmin.model.ClipboardPermission;
import com.ncc.savior.virtueadmin.model.ClipboardPermissionOption;

public class RestDataGuard implements ICrossGroupDataGuard {
	private static final Logger logger = LoggerFactory.getLogger(RestDataGuard.class);

	private DesktopResourceService resource;
	private Map<Pair<String, String>, ClipboardPermissionOption> cache;

	private PassiveExpiringMap<Pair<String, String>, ClipboardPermissionOption> tempCache;

	private HashMap<String, String> groupIdToDisplayName;

	/**
	 *
	 * @param resource
	 * @param askStickyTimeoutMillis
	 *            - When the {@link ClipboardPermissionOption} is set to ASK, we
	 *            only Ask once and then that option sticks for a period of time
	 *            equal to this parameter.
	 */
	public RestDataGuard(DesktopResourceService resource, long askStickyTimeoutMillis) {
		this.resource = resource;
		this.tempCache = new PassiveExpiringMap<Pair<String, String>, ClipboardPermissionOption>(
				askStickyTimeoutMillis);
	}

	@Override
	public void init() {
		updateCache();
	}

	private void updateCache() {
		List<ClipboardPermission> permissionsList = this.resource.getAllComputedPermissions();
		Map<Pair<String, String>, ClipboardPermissionOption> map = new TreeMap<Pair<String, String>, ClipboardPermissionOption>();
		for (ClipboardPermission p : permissionsList) {
			ImmutablePair<String, String> pair = new ImmutablePair<String, String>(p.getSourceGroupId(),
					p.getDestinationGroupId());
			map.put(pair, p.getPermission());
		}
		cache = map;
	}

	@Override
	public boolean allowDataTransfer(String dataSourceGroupId, String dataDestinationGroupId) {
		ImmutablePair<String, String> pair = new ImmutablePair<String, String>(dataSourceGroupId,
				dataDestinationGroupId);
		// try to avoid synchronized on as many calls as possible, but when we don't
		// have data, make sure we only update once.
		if (cache == null) {
			synchronized (this) {
				// This second if is in here so we only update once instead of blocking a bunch
				// of calls that will then all proceed to update the cache.
				if (cache == null) {
					updateCache();
				}
			}
		}
		ClipboardPermissionOption po = getPermissionOptionIncludingTemporary(pair);
		switch (po) {
		case ALLOW:
			return true;
		case ASK:
			int dialogButton = JOptionPane.YES_NO_OPTION;
			int dialogResult = JOptionPane.showConfirmDialog(null, "Would you like allow copy/pasting 15 minutes from '"
					+ getName(pair.left) + "' to '" + getName(pair.right) + "'?", "Warning", dialogButton);
			ClipboardPermissionOption tempOption = (dialogResult == JOptionPane.YES_OPTION
					? ClipboardPermissionOption.ALLOW
					: ClipboardPermissionOption.DENY);
			addToTemporary(pair, tempOption);
			return false;
		case DENY:
			return false;
		default:
			logger.error("Error getting permission option.  Option was=" + po);
			return false;
		}
	}

	private String getName(String groupId) {
		String displayName = groupIdToDisplayName.get(groupId);
		if (displayName == null) {
			displayName = "ID=" + groupId;
		}
		return displayName;
	}

	private void addToTemporary(ImmutablePair<String, String> pair, ClipboardPermissionOption tempOption) {
		tempCache.put(pair, tempOption);
	}

	/**
	 * Need to be careful as the map isn't synchronized
	 *
	 * @param pair
	 * @return
	 */
	private ClipboardPermissionOption getPermissionOptionIncludingTemporary(ImmutablePair<String, String> pair) {
		ClipboardPermissionOption po = cache.get(pair);
		if (ClipboardPermissionOption.ASK.equals(po)) {
			ClipboardPermissionOption temp = tempCache.get(pair);
			if (temp != null) {
				return temp;
			}
		}
		return po;
	}

	@Override
	public void setGroupIdToDisplayNameMap(HashMap<String, String> groupIdToDisplayName) {
		this.groupIdToDisplayName = groupIdToDisplayName;
	}

}
