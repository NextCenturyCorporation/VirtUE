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
package com.ncc.savior.virtueadmin.infrastructure.mixed;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.virtueadmin.infrastructure.IKeyManager;
import com.ncc.savior.virtueadmin.infrastructure.IUpdateListener;
import com.ncc.savior.virtueadmin.infrastructure.IVmUpdater;
import com.ncc.savior.virtueadmin.infrastructure.pipelining.IUpdatePipeline;
import com.ncc.savior.virtueadmin.infrastructure.pipelining.NetworkingClearingComponent;
import com.ncc.savior.virtueadmin.infrastructure.pipelining.TestReachabilityAndAddRsaComponent;
import com.ncc.savior.virtueadmin.infrastructure.pipelining.TestReachabilityComponent;
import com.ncc.savior.virtueadmin.infrastructure.pipelining.UpdatePipeline;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VmState;

/**
 * {@link IVmUpdater} specifically for Guest Virtual Machines (DomU) created in
 * Xen.
 * 
 *
 */
public class XenGuestVmUpdater implements IVmUpdater {
	private static final Logger logger = LoggerFactory.getLogger(XenHostVmUpdater.class);
	private ScheduledExecutorService executor;
	private IUpdatePipeline<VirtualMachine> provisionPipeline;
	private IUpdatePipeline<VirtualMachine> startingPipeline;
	private IUpdatePipeline<VirtualMachine> stoppingPipeline;

	public XenGuestVmUpdater(IUpdateListener<VirtualMachine> xenVmHostNotifier, IKeyManager keyManager) {
		this.provisionPipeline = new UpdatePipeline<VirtualMachine>(xenVmHostNotifier, "provisioning");
		this.startingPipeline = new UpdatePipeline<VirtualMachine>(xenVmHostNotifier, "starting");
		this.stoppingPipeline = new UpdatePipeline<VirtualMachine>(xenVmHostNotifier, "stopping");

		this.executor = Executors.newScheduledThreadPool(2, new ThreadFactory() {
			private int num = 1;

			@Override
			public synchronized Thread newThread(Runnable r) {
				String name = "xen-guest-updater-" + num;
				num++;
				return new Thread(r, name);
			}
		});

		TestReachabilityAndAddRsaComponent reachableRsa = new TestReachabilityAndAddRsaComponent(executor, keyManager,
				200);
		provisionPipeline.addPipelineComponent(reachableRsa);
		reachableRsa.setSuccessState(VmState.RUNNING);
		provisionPipeline.start();

		startingPipeline.addPipelineComponent(new TestReachabilityComponent(executor, keyManager, true, 5000));
		startingPipeline.start();

		stoppingPipeline.addPipelineComponent(new NetworkingClearingComponent(executor));
		Collection<VmState> successStatus = new ArrayList<VmState>();
		successStatus.add(VmState.DELETED);
		successStatus.add(VmState.STOPPED);
		stoppingPipeline.addPipelineComponent(new TestReachabilityComponent(executor, keyManager, false, 3000));
		stoppingPipeline.start();

		logger.debug("Xen guest update pipelines started");
	}

	@Override
	public void addVmToProvisionPipeline(Collection<VirtualMachine> vms) {
		logger.debug("added vms " + vms);
		provisionPipeline.addToPipeline(vms);
	}

	@Override
	public void addVmsToStartingPipeline(Collection<VirtualMachine> vms) {
		startingPipeline.addToPipeline(vms);

	}

	@Override
	public void addVmsToStoppingPipeline(Collection<VirtualMachine> vms) {
		stoppingPipeline.addToPipeline(vms);

	}

	@Override
	public void addVmsToDeletingPipeline(Collection<VirtualMachine> vms) {
		stoppingPipeline.addToPipeline(vms);
	}
}
