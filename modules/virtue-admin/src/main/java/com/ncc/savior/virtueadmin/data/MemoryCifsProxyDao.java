package com.ncc.savior.virtueadmin.data;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.ncc.savior.virtueadmin.cifsproxy.ICifsProxyDao;
import com.ncc.savior.virtueadmin.model.CifsProxyData;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VirtueUser;

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

	
}
