package com.ncc.savior.virtueadmin.cifsproxy;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.ncc.savior.virtueadmin.model.VirtueInstance;
import com.ncc.savior.virtueadmin.model.VirtueState;
import com.ncc.savior.virtueadmin.model.VirtueUser;
import com.ncc.savior.virtueadmin.virtue.IActiveVirtueManager;

public class VirtueTimeoutManager {
	private Map<String, Long> userTimeoutMap;
	private Long timeout;
	
	public VirtueTimeoutManager(Long timeout) {
		this.timeout = timeout;
		this.userTimeoutMap = new HashMap<String, Long>();
	}
	
	public void updateUserTimeout(VirtueUser user) {
		userTimeoutMap.put(user.getUsername(), System.currentTimeMillis() + timeout);
	}
	
	public long getTimeout() {
		return timeout;
	}
	
	private boolean userTimedOut(VirtueUser user) {
		if (timeout <= 0) {
			return false;
		} else {
			Long timeout = userTimeoutMap.get(user.getUsername());
			if (timeout == null) {
				updateUserTimeout(user);
				return false;
			} else {
				long current = System.currentTimeMillis();
				return (current > timeout);
			}
		}
	}

	public void testAndShutdownVirtues(VirtueUser user, IActiveVirtueManager activeVirtueManager) {
		Collection<VirtueInstance> vs = activeVirtueManager.getVirtuesForUser(user);
		
		if (userTimedOut(user)) {
			for (VirtueInstance instance : vs) {
				if (instance.getState() == VirtueState.RUNNING) {
					activeVirtueManager.stopVirtue(user, instance.getId());
				}
			}
		}
	}
}
