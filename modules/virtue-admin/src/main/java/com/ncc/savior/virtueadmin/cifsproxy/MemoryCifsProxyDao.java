package com.ncc.savior.virtueadmin.cifsproxy;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VirtueUser;

public class MemoryCifsProxyDao implements ICifsProxyDao {

	private HashMap<String, MemoryCifsData> userMap;
	private long timeoutDelayMillis;

	public MemoryCifsProxyDao(long timeoutDelayMillis) {
		this.userMap = new HashMap<String, MemoryCifsData>();
		this.timeoutDelayMillis = timeoutDelayMillis;
	}

	@Override
	public synchronized VirtualMachine getCifsVm(VirtueUser user) {
		MemoryCifsData data = userMap.get(user.getUsername());
		if (data == null) {
			return null;
		} else {
			return data.getCifsVm();
		}

	}

	@Override
	public synchronized void updateCifsVm(VirtueUser user, VirtualMachine vm) {
		MemoryCifsData data = userMap.get(user.getUsername());
		if (data == null) {
			data = new MemoryCifsData(user, vm, getTimeoutTimeFromNow());
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
		MemoryCifsData data = userMap.get(user.getUsername());
		if (data != null) {
			data.setTimeoutMillis(getTimeoutTimeFromNow());
		}
		userMap.put(user.getUsername(), data);
	}

	@Override
	public synchronized long getUserTimeout(VirtueUser user) {
		MemoryCifsData data = userMap.get(user.getUsername());
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
		for (MemoryCifsData entry : userMap.values()) {
			set.add(entry.getUser());
		}
		return set;
	}

	private class MemoryCifsData {
		private VirtueUser user;
		private VirtualMachine cifsVm;
		private long timeoutMillis;

		protected MemoryCifsData(VirtueUser user, VirtualMachine cifsVm, long timeoutMillis) {
			super();
			this.user = user;
			this.cifsVm = cifsVm;
			this.timeoutMillis = timeoutMillis;
		}

		public VirtueUser getUser() {
			return user;
		}

		public void setUser(VirtueUser user) {
			this.user = user;
		}

		public VirtualMachine getCifsVm() {
			return cifsVm;
		}

		public void setCifsVm(VirtualMachine cifsVm) {
			this.cifsVm = cifsVm;
		}

		public long getTimeoutMillis() {
			return timeoutMillis;
		}

		public void setTimeoutMillis(long timeoutMillis) {
			this.timeoutMillis = timeoutMillis;
		}
	}
}
