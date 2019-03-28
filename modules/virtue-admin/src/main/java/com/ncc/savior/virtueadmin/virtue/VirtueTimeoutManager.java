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
package com.ncc.savior.virtueadmin.virtue;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
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

public class VirtueTimeoutManager {
	@Autowired
	private IUserManager userManager;
	private Map<String, Long> userTimeoutMap;
	private Long timeout;
	private IActiveVirtueManager activeVirtueManager;
	
	public VirtueTimeoutManager(Long timeout, IActiveVirtueManager activeVirtueManager,
			DesktopVirtueService desktopService, CompletableFutureServiceProvider serviceProvider) {
		this.timeout = timeout;
		this.userTimeoutMap = new ConcurrentHashMap<String, Long>();
		this.activeVirtueManager = activeVirtueManager;
				
		serviceProvider.getExecutor().scheduleWithFixedDelay(getTestForUserTimeoutRunnable(), 10000, 5000,
				TimeUnit.MILLISECONDS);
		
		desktopService.addPollHandler(new PollHandler() {

			@Override
			public void onPoll(VirtueUser user, Map<String, VirtueTemplate> templates,
					Map<String, Set<VirtueInstance>> templateIdToActiveVirtues) {
				updateUserTimeout(user.getUsername());
			}
		});
	}
	
	public void updateUserTimeout(String username) {
		userTimeoutMap.put(username, System.currentTimeMillis() + timeout);
	}
	
	public long getTimeout() {
		return timeout;
	}
	
	private boolean userTimedOut(String username) {
		if (timeout <= 0) {
			return false;
		} else {
			Long timeout = userTimeoutMap.get(username);
			// prevents nullPointer when server starts
			if (timeout == null) {
				updateUserTimeout(username);
				return false;
			} else {
				long current = System.currentTimeMillis();
				return (current > timeout);
			}
		}
	}

	public void testAndShutdownVirtues(String username) {
		if (userTimedOut(username)) {
			VirtueUser user = userManager.getUser(username);
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
