package com.ncc.savior.virtueadmin.infrastructure.mixed;

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
import com.ncc.savior.virtueadmin.infrastructure.pipelining.IUpdatePipeline;
import com.ncc.savior.virtueadmin.infrastructure.pipelining.NetworkingClearingComponent;
import com.ncc.savior.virtueadmin.infrastructure.pipelining.SetStatusComponent;
import com.ncc.savior.virtueadmin.infrastructure.pipelining.TestReachabilityAndAddRsaComponent;
import com.ncc.savior.virtueadmin.infrastructure.pipelining.TestReachabilityComponent;
import com.ncc.savior.virtueadmin.infrastructure.pipelining.UpdatePipeline;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VmState;

public class XenHostCreationVmUpdater implements IVmUpdater {
	private static final Logger logger = LoggerFactory.getLogger(XenHostCreationVmUpdater.class);
	private ScheduledExecutorService executor;
	private IUpdatePipeline<VirtualMachine> provisionPipeline;
	private IUpdatePipeline<VirtualMachine> startingPipeline;
	private IUpdatePipeline<VirtualMachine> stoppingPipeline;
	private AmazonEC2 ec2;
	private IUpdateListener<VirtualMachine> notifier;
	private IKeyManager keyManager;

	public XenHostCreationVmUpdater(AmazonEC2 ec2, IUpdateListener<VirtualMachine> notifier, IKeyManager keyManager) {
		this.ec2 = ec2;
		this.notifier = notifier;
		this.keyManager = keyManager;
		this.provisionPipeline = new UpdatePipeline<VirtualMachine>(notifier, "provisioning");
		this.startingPipeline = new UpdatePipeline<VirtualMachine>(notifier, "starting");
		this.stoppingPipeline = new UpdatePipeline<VirtualMachine>(notifier, "stopping");

		this.executor = Executors.newScheduledThreadPool(2, new ThreadFactory() {
			private int num = 1;

			@Override
			public synchronized Thread newThread(Runnable r) {
				String name = "xen-updated-" + num;
				num++;
				return new Thread(r, name);
			}
		});
		provisionPipeline.addPipelineComponent(new AwsRenamingComponent(executor, ec2));
		provisionPipeline.addPipelineComponent(new AwsNetworkingUpdateComponent(executor, ec2));
		TestReachabilityAndAddRsaComponent reachableRsa = new TestReachabilityAndAddRsaComponent(executor, keyManager);
		provisionPipeline.addPipelineComponent(reachableRsa);
		reachableRsa.setSuccessState(VmState.RUNNING);
		provisionPipeline.start();

		startingPipeline.addPipelineComponent(new AwsNetworkingUpdateComponent(executor, ec2));
		startingPipeline.addPipelineComponent(new TestReachabilityComponent(executor, keyManager, true));
		startingPipeline.start();

		stoppingPipeline.addPipelineComponent(new NetworkingClearingComponent(executor));
		stoppingPipeline.addPipelineComponent(new TestReachabilityComponent(executor, keyManager, false));
		stoppingPipeline.addPipelineComponent(new SetStatusComponent(executor, VmState.STOPPED));
		stoppingPipeline.start();

		logger.debug("Aws update pipelines started");
	}

	@Override
	public void addVmToProvisionPipeline(ArrayList<VirtualMachine> vms) {
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
