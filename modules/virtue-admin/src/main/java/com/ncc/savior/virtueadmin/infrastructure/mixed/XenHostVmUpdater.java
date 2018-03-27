package com.ncc.savior.virtueadmin.infrastructure.mixed;

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
import com.ncc.savior.virtueadmin.infrastructure.pipelining.IUpdatePipeline;
import com.ncc.savior.virtueadmin.infrastructure.pipelining.NetworkingClearingComponent;
import com.ncc.savior.virtueadmin.infrastructure.pipelining.SetStatusComponent;
import com.ncc.savior.virtueadmin.infrastructure.pipelining.TestReachabilityAndAddRsaComponent;
import com.ncc.savior.virtueadmin.infrastructure.pipelining.TestReachabilityComponent;
import com.ncc.savior.virtueadmin.infrastructure.pipelining.UpdatePipeline;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VmState;

public class XenHostVmUpdater implements IVmUpdater {
	private static final Logger logger = LoggerFactory.getLogger(XenHostVmUpdater.class);
	private ScheduledExecutorService executor;
	private IUpdatePipeline<VirtualMachine> zenVmProvisionPipeline;
	private IUpdatePipeline<VirtualMachine> startingPipeline;
	private IUpdatePipeline<VirtualMachine> stoppingPipeline;

	public XenHostVmUpdater(AmazonEC2 ec2, IUpdateListener<VirtualMachine> xenVmHostNotifier,
			IKeyManager keyManager) {
		this.zenVmProvisionPipeline = new UpdatePipeline<VirtualMachine>(xenVmHostNotifier, "provisioning");
		this.startingPipeline = new UpdatePipeline<VirtualMachine>(xenVmHostNotifier, "starting");
		this.stoppingPipeline = new UpdatePipeline<VirtualMachine>(xenVmHostNotifier, "stopping");

		this.executor = Executors.newScheduledThreadPool(2, new ThreadFactory() {
			private int num = 1;

			@Override
			public synchronized Thread newThread(Runnable r) {
				String name = "xen-updated-" + num;
				num++;
				return new Thread(r, name);
			}
		});

		zenVmProvisionPipeline.addPipelineComponent(new AwsRenamingComponent(executor, ec2));
		zenVmProvisionPipeline.addPipelineComponent(new AwsNetworkingUpdateComponent(executor, ec2));
		TestReachabilityAndAddRsaComponent reachableRsa = new TestReachabilityAndAddRsaComponent(executor, keyManager);
		zenVmProvisionPipeline.addPipelineComponent(reachableRsa);
		reachableRsa.setSuccessState(VmState.RUNNING);
		zenVmProvisionPipeline.start();

		startingPipeline.addPipelineComponent(
				new AwsNetworkingUpdateComponent(executor, ec2));
		startingPipeline.addPipelineComponent(
				new TestReachabilityComponent(executor, keyManager, true));
		startingPipeline.start();

		stoppingPipeline
				.addPipelineComponent(new NetworkingClearingComponent(executor));
		stoppingPipeline.addPipelineComponent(
				new TestReachabilityComponent(executor, keyManager, false));
		stoppingPipeline.addPipelineComponent(
				new SetStatusComponent(executor, VmState.STOPPED));
		stoppingPipeline.start();

		logger.debug("Aws update pipelines started");
	}

	@Override
	public void addVmToProvisionPipeline(Collection<VirtualMachine> vms) {
		zenVmProvisionPipeline.addToPipeline(vms);
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
