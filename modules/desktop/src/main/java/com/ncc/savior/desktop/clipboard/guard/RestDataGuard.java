package com.ncc.savior.desktop.clipboard.guard;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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

	public RestDataGuard(DesktopResourceService resource) {
		this.resource = resource;
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
	public ClipboardPermissionOption allowDataTransfer(String dataSourceGroupId, String dataDestinationGroupId) {
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
		ClipboardPermissionOption po = cache.get(pair);
		return po;
		// switch (po) {
		// case ALLOW:
		// return true;
		// case ASK:
		// int dialogButton = JOptionPane.YES_NO_OPTION;
		// int dialogResult = JOptionPane.showConfirmDialog(null, "Would you like to
		// copy this data?", "Warning",
		// dialogButton);
		// if (dialogResult == JOptionPane.YES_OPTION) {
		// return true;
		// }
		// return false;
		// case DENY:
		// return false;
		// default:
		// logger.error("Error getting permission option. Option was=" + po);
		// return false;
		// }
	}

}
