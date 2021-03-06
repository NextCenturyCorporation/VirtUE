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
package com.ncc.savior.virtueadmin.data;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.ncc.savior.virtueadmin.cifsproxy.ICifsProxyDao;
import com.ncc.savior.virtueadmin.model.CifsProxyData;
import com.ncc.savior.virtueadmin.model.CifsShareCreationParameter;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VirtueUser;

/**
 * Test Cifs DAO.  Not used in production and may have unimplemented methods. 
 *
 */
public class MemoryCifsProxyDao implements ICifsProxyDao {

	private HashMap<String, CifsProxyData> userMap;
	private long timeoutDelayMillis;

	public MemoryCifsProxyDao(long timeoutDelayMillis) {
		this.userMap = new HashMap<String, CifsProxyData>();
		this.timeoutDelayMillis = timeoutDelayMillis;
	}

	@Override
	public synchronized VirtualMachine getCifsVm(VirtueUser user) {
		CifsProxyData data = userMap.get(user.getUsername());
		if (data == null) {
			return null;
		} else {
			return data.getCifsVm();
		}
	}

	@Override
	public synchronized void updateCifsVm(VirtueUser user, VirtualMachine vm) {
		CifsProxyData data = userMap.get(user.getUsername());
		if (data == null) {
			data = new CifsProxyData(user, vm, getTimeoutTimeFromNow());
		} else {
			data.setCifsVm(vm);
		}
		userMap.put(user.getUsername(), data);
	}

	private long getTimeoutTimeFromNow() {
		return System.currentTimeMillis() + timeoutDelayMillis;
	}

	@Override
	public synchronized void updateUserTimeout(VirtueUser user) {
		CifsProxyData data = userMap.get(user.getUsername());
		if (data != null) {
			data.setTimeoutMillis(getTimeoutTimeFromNow());
		}
		userMap.put(user.getUsername(), data);
	}

	@Override
	public synchronized long getUserTimeout(VirtueUser user) {
		CifsProxyData data = userMap.get(user.getUsername());
		if (data != null) {
			return data.getTimeoutMillis();
		}
		return -1;
	}

	@Override
	public synchronized void deleteCifsVm(VirtueUser user) {
		userMap.remove(user.getUsername());
	}

	@Override
	public synchronized Set<VirtueUser> getAllUsers() {
		HashSet<VirtueUser> set = new HashSet<VirtueUser>();
		for (CifsProxyData entry : userMap.values()) {
			set.add(entry.getUser());
		}
		return set;
	}

	@Override
	public Collection<VirtualMachine> getAllCifsVms() {
		HashSet<VirtualMachine> set = new HashSet<VirtualMachine>();
		for (CifsProxyData entry : userMap.values()) {
			set.add(entry.getCifsVm());
		}
		return set;
	}

	@Override
	public void saveShareParams(CifsShareCreationParameter share) {
		throw notImplemented();
	}

	@Override
	public List<CifsShareCreationParameter> getSharesForVirtue(String virtueId) {
		throw notImplemented();
	}

	private RuntimeException notImplemented() {
		throw new RuntimeException("Not implemented");
	}

}
