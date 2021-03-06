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
package com.ncc.savior.virtueadmin.infrastructure.aws;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.ec2.AmazonEC2;
import com.ncc.savior.virtueadmin.infrastructure.IKeyManager;
import com.ncc.savior.virtueadmin.infrastructure.IUpdateListener;
import com.ncc.savior.virtueadmin.infrastructure.IVmUpdater;
import com.ncc.savior.virtueadmin.infrastructure.pipelining.AwsNetworkingUpdateComponent;
import com.ncc.savior.virtueadmin.infrastructure.pipelining.AwsRenamingComponent;
import com.ncc.savior.virtueadmin.infrastructure.pipelining.EnsureDeleteVolumeOnTerminationUpdateComponent;
import com.ncc.savior.virtueadmin.infrastructure.pipelining.IUpdatePipeline;
import com.ncc.savior.virtueadmin.infrastructure.pipelining.NetworkingClearingComponent;
import com.ncc.savior.virtueadmin.infrastructure.pipelining.StartXpraComponent;
import com.ncc.savior.virtueadmin.infrastructure.pipelining.TestReachabilityAndAddRsaComponent;
import com.ncc.savior.virtueadmin.infrastructure.pipelining.TestReachabilityComponent;
import com.ncc.savior.virtueadmin.infrastructure.pipelining.UpdatePipeline;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VmState;
import com.ncc.savior.virtueadmin.template.ITemplateService;

/**
 * {@link IVmUpdater} implemented designed specifically for AWS EC2 VM's that
 * are intended to be VM's in the Savior system (as opposed to Xen Hosts).
 * 
 *
 */
public class AwsVmUpdater implements IVmUpdater {
	private static final Logger logger = LoggerFactory.getLogger(AwsVmUpdater.class);
	private ScheduledExecutorService executor;
	private IUpdatePipeline<VirtualMachine> provisionPipeline;
	private IUpdatePipeline<VirtualMachine> startingPipeline;
	private IUpdatePipeline<VirtualMachine> stoppingPipeline;

	public AwsVmUpdater(VirtueAwsEc2Provider ec2Provider, IUpdateListener<VirtualMachine> notifier,
			IKeyManager keyManager, boolean includeXpra, boolean changePrivateKey, boolean usePublicDns,
			ITemplateService templateService) {
		AmazonEC2 ec2 = ec2Provider.getEc2();
		this.provisionPipeline = new UpdatePipeline<VirtualMachine>(notifier, "provisioning");
		this.startingPipeline = new UpdatePipeline<VirtualMachine>(notifier, "starting");
		this.stoppingPipeline = new UpdatePipeline<VirtualMachine>(notifier, "stopping");
		this.executor = Executors.newScheduledThreadPool(3, new ThreadFactory() {
			private int num = 1;

			@Override
			public synchronized Thread newThread(Runnable r) {
				String name = "aws-updater-" + num;
				num++;
				return new Thread(r, name);
			}
		});
		provisionPipeline.addPipelineComponent(new AwsRenamingComponent(executor, ec2));
		provisionPipeline.addPipelineComponent(new AwsNetworkingUpdateComponent(executor, ec2, usePublicDns));
		provisionPipeline.addPipelineComponent(new EnsureDeleteVolumeOnTerminationUpdateComponent(executor, ec2));
		TestReachabilityAndAddRsaComponent reachableRsa = new TestReachabilityAndAddRsaComponent(executor, keyManager,
				false);
		provisionPipeline.addPipelineComponent(reachableRsa);
		if (includeXpra) {
			reachableRsa.setSuccessState(VmState.LAUNCHING);
			provisionPipeline.addPipelineComponent(new StartXpraComponent(executor, keyManager, templateService));
		} else {
			reachableRsa.setSuccessState(VmState.RUNNING);
		}
		provisionPipeline.start();

		startingPipeline.addPipelineComponent(new AwsNetworkingUpdateComponent(executor, ec2, usePublicDns));
		startingPipeline.addPipelineComponent(new TestReachabilityComponent(executor, keyManager, true, 5000));
		startingPipeline.start();

		stoppingPipeline.addPipelineComponent(new NetworkingClearingComponent(executor));
		Collection<VmState> successStatus = new ArrayList<VmState>();
		successStatus.add(VmState.DELETED);
		successStatus.add(VmState.STOPPED);
		stoppingPipeline.addPipelineComponent(new AwsUpdateStatus(executor, ec2, successStatus));
		stoppingPipeline.start();

		logger.debug("Aws update pipelines started");
		// startDebug();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ncc.savior.virtueadmin.infrastructure.aws.IVmUpdater#
	 * addVmToProvisionPipeline(java.util.ArrayList)
	 */
	@Override
	public void addVmToProvisionPipeline(Collection<VirtualMachine> vms) {
		provisionPipeline.addToPipeline(vms);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ncc.savior.virtueadmin.infrastructure.aws.IVmUpdater#
	 * addVmsToStartingPipeline(java.util.Collection)
	 */
	@Override
	public void addVmsToStartingPipeline(Collection<VirtualMachine> vms) {
		startingPipeline.addToPipeline(vms);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ncc.savior.virtueadmin.infrastructure.aws.IVmUpdater#
	 * addVmsToStoppingPipeline(java.util.Collection)
	 */
	@Override
	public void addVmsToStoppingPipeline(Collection<VirtualMachine> vms) {
		stoppingPipeline.addToPipeline(vms);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ncc.savior.virtueadmin.infrastructure.aws.IVmUpdater#
	 * addVmsToDeletingPipeline(java.util.Collection)
	 */
	@Override
	public void addVmsToDeletingPipeline(Collection<VirtualMachine> vms) {
		stoppingPipeline.addToPipeline(vms);
	}

}
