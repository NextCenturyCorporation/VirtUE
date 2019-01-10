package com.ncc.savior.virtueadmin.cifsproxy;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;

import com.ncc.savior.virtueadmin.data.IUserManager;
import com.ncc.savior.virtueadmin.infrastructure.future.CompletableFutureServiceProvider;
import com.ncc.savior.virtueadmin.model.VirtueInstance;
import com.ncc.savior.virtueadmin.model.VirtueState;
import com.ncc.savior.virtueadmin.model.VirtueTemplate;
import com.ncc.savior.virtueadmin.model.VirtueUser;
import com.ncc.savior.virtueadmin.service.DesktopVirtueService;
import com.ncc.savior.virtueadmin.service.DesktopVirtueService.PollHandler;
import com.ncc.savior.virtueadmin.virtue.IActiveVirtueManager;

public class VirtueTimeoutManager {
	@Autowired
	private IUserManager userManager;
	private Map<String, Long> userTimeoutMap;
	private Long timeout;
	private IActiveVirtueManager activeVirtueManager;
	
	public VirtueTimeoutManager(Long timeout, IActiveVirtueManager activeVirtueManager,
			DesktopVirtueService desktopService, CompletableFutureServiceProvider serviceProvider) {
		this.timeout = timeout;
		this.userTimeoutMap = Collections.synchronizedMap(new HashMap<String, Long>());
		this.activeVirtueManager = activeVirtueManager;
				
		serviceProvider.getExecutor().scheduleWithFixedDelay(getTestForUserTimeoutRunnable(), 7000, 1000,
				TimeUnit.MILLISECONDS);
		
		desktopService.addPollHandler(new PollHandler() {

			@Override
			public void onPoll(VirtueUser user, Map<String, VirtueTemplate> templates,
					Map<String, Set<VirtueInstance>> templateIdToActiveVirtues) {
				updateUserTimeout(user);
			}
		});
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
			// prevents nullPointer when server starts
			if (timeout == null) {
				updateUserTimeout(user);
				return false;
			} else {
				long current = System.currentTimeMillis();
				return (current > timeout);
			}
		}
	}

	public void testAndShutdownVirtues(String username) {
		VirtueUser user = userManager.getUser(username);
		if (userTimedOut(user)) {
			Collection<VirtueInstance> vs = activeVirtueManager.getVirtuesForUser(user);
			for (VirtueInstance instance : vs) {
				if (instance.getState() == VirtueState.RUNNING) {
					activeVirtueManager.stopVirtue(user, instance.getId());
				}
			}
		}
	}
	
	private Runnable getTestForUserTimeoutRunnable() {
		return new Runnable() {

			@Override
			public void run() {
				Set<String> users = userTimeoutMap.keySet();
				for (String user : users) {
					testAndShutdownVirtues(user);
				}
			}
		};
	}
}
