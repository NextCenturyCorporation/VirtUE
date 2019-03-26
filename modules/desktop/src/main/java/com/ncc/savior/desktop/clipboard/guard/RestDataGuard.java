/*
 * Copyright (C) 2019 Next Century Corporation
 * 
 * This file may be redistributed and/or modified under either the GPL
 * 2.0 or 3-Clause BSD license. In addition, the U.S. Government is
 * granted government purpose rights. For details, see the COPYRIGHT.TXT
 * file at the root of this project.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 * 
 * SPDX-License-Identifier: (GPL-2.0-only OR BSD-3-Clause)
 */
package com.ncc.savior.desktop.clipboard.guard;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.collections4.map.PassiveExpiringMap;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.clipboard.guard.CopyPasteDialog.IDialogListener;
import com.ncc.savior.desktop.virtues.DesktopResourceService;
import com.ncc.savior.virtueadmin.model.ClipboardPermission;
import com.ncc.savior.virtueadmin.model.ClipboardPermissionOption;

/**
 * {@link ICrossGroupDataGuard} implementation that pulls
 * {@link ClipboardPermissionOption}s from a rest service. This guard pulls the
 * data down once and caches that data. It does not update the cache. For
 * {@link ClipboardPermission}s that are set as
 * {@link ClipboardPermissionOption#ASK}, there is an timeout cache that keeps
 * the users responses for a period of time.
 */
public class RestDataGuard implements ICrossGroupDataGuard {
	private static final Logger logger = LoggerFactory.getLogger(RestDataGuard.class);

	private DesktopResourceService resource;
	private Map<Pair<String, String>, ClipboardPermissionOption> cache;

	private PassiveExpiringMap<Pair<String, String>, ClipboardPermissionOption> tempCache;

	private Map<String, String> groupIdToDisplayName;

	private IDataGuardDialog dialog;

	/**
	 *
	 * @param resource
	 * @param askStickyTimeoutMillis
	 *            - When the {@link ClipboardPermissionOption} is set to ASK, we
	 *            only Ask once and then that option sticks for a period of time
	 *            equal to this parameter.
	 */
	public RestDataGuard(DesktopResourceService resource, long askStickyTimeoutMillis, IDataGuardDialog dialog) {
		this.resource = resource;
		this.tempCache = new PassiveExpiringMap<Pair<String, String>, ClipboardPermissionOption>(
				askStickyTimeoutMillis);
		this.dialog = dialog;
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
			dialog.setDialogListener(new IDialogListener() {

				@Override
				public void onYes() {
					addToTemporaryCache(pair, ClipboardPermissionOption.ALLOW);
				}

				@Override
				public void onNo() {
					addToTemporaryCache(pair, ClipboardPermissionOption.DENY);
				}

			});
			dialog.show(getName(pair.left), getName(pair.right));
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

	private void addToTemporaryCache(ImmutablePair<String, String> pair, ClipboardPermissionOption tempOption) {
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
	public void setGroupIdToDisplayNameMap(Map<String, String> groupIdToDisplayName) {
		this.groupIdToDisplayName = groupIdToDisplayName;
	}
}
