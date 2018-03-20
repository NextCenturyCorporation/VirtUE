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
import com.ncc.savior.virtueadmin.infrastructure.pipelining.AwsGetStatusComponent;
import com.ncc.savior.virtueadmin.infrastructure.pipelining.AwsNetworkingUpdateComponent;
import com.ncc.savior.virtueadmin.infrastructure.pipelining.AwsRenamingComponent;
import com.ncc.savior.virtueadmin.infrastructure.pipelining.IUpdatePipeline;
import com.ncc.savior.virtueadmin.infrastructure.pipelining.NetworkingClearingComponent;
import com.ncc.savior.virtueadmin.infrastructure.pipelining.StartXpraComponent;
import com.ncc.savior.virtueadmin.infrastructure.pipelining.TestReachabilityAndAddRsaComponent;
import com.ncc.savior.virtueadmin.infrastructure.pipelining.TestReachabilityComponent;
import com.ncc.savior.virtueadmin.infrastructure.pipelining.UpdatePipeline;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VmState;

public class AwsVmUpdater {
	private static final Logger logger = LoggerFactory.getLogger(AwsVmUpdater.class);
	private ScheduledExecutorService executor;
	private IUpdatePipeline provisionPipeline;
	private IUpdatePipeline startingPipeline;
	private IUpdatePipeline stoppingPipeline;

	public AwsVmUpdater(AmazonEC2 ec2, IUpdateNotifier notifier, IKeyManager keyManager) {
		this.provisionPipeline = new UpdatePipeline(notifier, "provisioning");
		this.startingPipeline = new UpdatePipeline(notifier, "starting");
		this.stoppingPipeline = new UpdatePipeline(notifier, "stopping");
		this.executor = Executors.newScheduledThreadPool(3, new ThreadFactory() {
			private int num = 1;
			@Override
			public synchronized Thread newThread(Runnable r) {
				String name = "aws-updated-" + num;
				num++;
				return new Thread(r, name);
			}
		});
		ArrayList<VmState> awsStartedStates = new ArrayList<VmState>();
		awsStartedStates.add(VmState.RUNNING);
		awsStartedStates.add(VmState.LAUNCHING);

		provisionPipeline.addPipelineComponent(new AwsRenamingComponent(executor, ec2));
		provisionPipeline.addPipelineComponent(new AwsNetworkingUpdateComponent(executor, ec2));
		provisionPipeline.addPipelineComponent(new AwsGetStatusComponent(executor, ec2, awsStartedStates));
		provisionPipeline.addPipelineComponent(new TestReachabilityAndAddRsaComponent(executor, keyManager));
		provisionPipeline.addPipelineComponent(new StartXpraComponent(executor, keyManager));
		provisionPipeline.start();

		startingPipeline.addPipelineComponent(new AwsNetworkingUpdateComponent(executor, ec2));
		startingPipeline.addPipelineComponent(new AwsGetStatusComponent(executor, ec2, awsStartedStates));
		startingPipeline.addPipelineComponent(new TestReachabilityComponent(executor, keyManager, true));
		startingPipeline.addPipelineComponent(new StartXpraComponent(executor, keyManager));
		startingPipeline.start();

		stoppingPipeline.addPipelineComponent(new NetworkingClearingComponent(executor));
		stoppingPipeline.addPipelineComponent(new TestReachabilityComponent(executor, keyManager, false));
		stoppingPipeline.addPipelineComponent(new AwsGetStatusComponent(executor, ec2, VmState.STOPPED));
		stoppingPipeline.start();

		logger.debug("Aws update pipelines started");
		// startDebug();
	}

	// private void startDebug() {
	// Runnable command = new Runnable() {
	// @SuppressWarnings("rawtypes")
	// @Override
	// public void run() {
	// ArrayList<VirtualMachine> net = new
	// ArrayList<VirtualMachine>(networkingQueue);
	// HashMap<String, ScheduledFuture> naming = new HashMap<String,
	// ScheduledFuture>(namingFutureMap);
	// HashMap<String, ScheduledFuture> reachable = new HashMap<String,
	// ScheduledFuture>(reachableFutureMap);
	// StringBuilder sb = new StringBuilder();
	// sb.append("AWS UPDATER STATUS:\n").append("Naming:\n");
	// int i = 1;
	// for (Entry<String, ScheduledFuture> entry : naming.entrySet()) {
	// sb.append(" ").append(i).append(". ").append(entry.getKey()).append(" -
	// ").append(entry.getValue())
	// .append("\n");
	// i++;
	// }
	// i = 1;
	// sb.append("Networking:\n");
	// for (VirtualMachine n : net) {
	// sb.append(" ").append(i).append(". ").append(n).append("\n");
	// i++;
	// }
	// i = 1;
	// sb.append("Reachability:\n");
	// for (Entry<String, ScheduledFuture> entry : reachable.entrySet()) {
	// sb.append(" ").append(i).append(". ").append(entry.getKey()).append(" -
	// ").append(entry.getValue())
	// .append("\n");
	// i++;
	// }
	// i = 1;
	// sb.append("Xpra:\n");
	// for (Entry<String, ScheduledFuture> entry : startXpraFutureMap.entrySet()) {
	// sb.append(" ").append(i).append(". ").append(entry.getKey()).append(" -
	// ").append(entry.getValue())
	// .append("\n");
	// i++;
	// }
	// logger.debug(sb.toString());
	// }
	// };
	// executor.scheduleAtFixedRate(command, 500, 5000, TimeUnit.MILLISECONDS);
	//
	// }

	/**
	 * Adds VMs to the provisioning pipeline. The provisioning pipeline leads the
	 * VMs through a series of tasks each at individual rates. The tasks are:
	 * <ol>
	 * <li>Rename AWS VM
	 * <li>Get networking information from AWS
	 * <li>Test reachability of VM and then add unique RSA key
	 * <li>Start Xpra server
	 * 
	 * @param vms
	 */
	public void addVmToProvisionPipeline(ArrayList<VirtualMachine> vms) {
		provisionPipeline.addToPipeline(vms);
	}

	public static interface IUpdateNotifier {
		void notifyUpdatedVms(Collection<VirtualMachine> vm);

		void notifyUpdatedVm(VirtualMachine vm);
	}

	public void addVmsToStartingPipeline(Collection<VirtualMachine> vms) {
		startingPipeline.addToPipeline(vms);

	}

	public void addVmsToStoppingPipeline(Collection<VirtualMachine> vms) {
		stoppingPipeline.addToPipeline(vms);

	}

	public void addVmsToDeletingPipeline(Collection<VirtualMachine> vms) {
		stoppingPipeline.addToPipeline(vms);
	}

}
