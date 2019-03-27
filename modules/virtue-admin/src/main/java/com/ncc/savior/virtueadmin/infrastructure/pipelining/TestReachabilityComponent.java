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
package com.ncc.savior.virtueadmin.infrastructure.pipelining;

import java.io.File;
import java.util.concurrent.ScheduledExecutorService;

import com.ncc.savior.util.SshUtil;
import com.ncc.savior.virtueadmin.infrastructure.IKeyManager;
import com.ncc.savior.virtueadmin.model.VirtualMachine;

/**
 * Component of an {@link IUpdatePipeline} that will test for reachability via
 * SSH. Success could be being reachable or unreachable depending on the
 * constructor parameter 'successOnReachable'. It will use
 * {@link VirtualMachine#getPrivateKeyName()} and a passin {@link IKeyManager}
 * to login.
 */
public class TestReachabilityComponent extends BaseIndividualVmPipelineComponent<VirtualMachine> {
	private IKeyManager keyManager;
	private boolean successOnReachable;

	public TestReachabilityComponent(ScheduledExecutorService executor, IKeyManager keyManager,
			boolean successOnReachable) {
		super(executor, true, 25000, 3000);
		this.keyManager = keyManager;
		this.successOnReachable = successOnReachable;
	}

	public TestReachabilityComponent(ScheduledExecutorService executor, IKeyManager keyManager,
			boolean successOnReachable, long initialDelay) {
		super(executor, true, initialDelay, 3000);
		this.keyManager = keyManager;
		this.successOnReachable = successOnReachable;
	}

	@Override
	protected void onExecute(PipelineWrapper<VirtualMachine> wrapper) {
		testReachability(wrapper);
	}

	/**
	 * Tests the reachability of a VM and calls success if reachable is desired
	 * value.
	 * 
	 * @param vm
	 */
	protected void testReachability(PipelineWrapper<VirtualMachine> wrapper) {
		VirtualMachine vm = wrapper.get();
		File privateKeyFile = keyManager.getKeyFileByName(vm.getPrivateKeyName());
		boolean reachable = SshUtil.isVmReachable(vm, privateKeyFile);
		if (reachable && successOnReachable) {
			doOnSuccess(wrapper);
		} else if (!reachable && !successOnReachable) {
			doOnSuccess(wrapper);
		}
	}

	@Override
	protected String getId(VirtualMachine element) {
		return element.getId();
	}
}
