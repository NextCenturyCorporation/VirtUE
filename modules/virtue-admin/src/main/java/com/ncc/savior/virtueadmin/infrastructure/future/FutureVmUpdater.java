package com.ncc.savior.virtueadmin.infrastructure.future;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.ec2.AmazonEC2;
import com.ncc.savior.virtueadmin.infrastructure.IKeyManager;
import com.ncc.savior.virtueadmin.infrastructure.IUpdateListener;
import com.ncc.savior.virtueadmin.infrastructure.IVmUpdater;
import com.ncc.savior.virtueadmin.infrastructure.aws.VirtueAwsEc2Provider;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VmState;
import com.ncc.savior.virtueadmin.util.JavaUtil;

public class FutureVmUpdater implements IVmUpdater {
	private static final Logger logger = LoggerFactory.getLogger(FutureVmUpdater.class);
	private ScheduledExecutorService executor;
	private AwsRenamingCompletableFutureService awsRenamingService;
	private AwsNetworkingUpdateService awsNetworkingUpdateService;
	private EnsureDeleteVolumeOnTerminationCompletableFutureService ensureDeleteVolumeOnTermination;
	private TestReachabilityCompletableFuture testUpDown;
	private AddRsaKeyCompletableFutureService addRsa;
	private BaseImediateCompletableFutureService<VirtualMachine, VirtualMachine, VmState> updateStatus;
	private BaseImediateCompletableFutureService<VirtualMachine, VirtualMachine, Void> notifierService;
	private BaseImediateCompletableFutureService<VirtualMachine, VirtualMachine, Void> networkClearingService;
	private AwsUpdateStatusCompletableFutureService awsUpdateStatus;

	public FutureVmUpdater(VirtueAwsEc2Provider ec2Provider, IUpdateListener<VirtualMachine> notifier,
			IKeyManager keyManager, boolean includeXpra, boolean changePrivateKey, boolean usePublicDns) {
		this.executor = Executors.newScheduledThreadPool(3, new ThreadFactory() {
			private int num = 1;

			@Override
			public synchronized Thread newThread(Runnable r) {
				String name = "aws-updater-" + num;
				num++;
				return new Thread(r, name);
			}
		});
		AmazonEC2 ec2 = ec2Provider.getEc2();
		awsRenamingService = new AwsRenamingCompletableFutureService(executor, ec2);
		awsNetworkingUpdateService = new AwsNetworkingUpdateService(executor, ec2, usePublicDns);
		ensureDeleteVolumeOnTermination = new EnsureDeleteVolumeOnTerminationCompletableFutureService(executor, ec2);
		testUpDown = new TestReachabilityCompletableFuture(executor, keyManager);
		addRsa = new AddRsaKeyCompletableFutureService(executor, keyManager);
		awsUpdateStatus = new AwsUpdateStatusCompletableFutureService(executor, ec2);
		updateStatus = new BaseImediateCompletableFutureService<VirtualMachine, VirtualMachine, VmState>(executor,
				"alterStatus") {
			@Override
			protected VirtualMachine onExecute(VirtualMachine param, VmState state) {
				param.setState(state);
				return param;
			}
		};
		notifierService = new BaseImediateCompletableFutureService<VirtualMachine, VirtualMachine, Void>(executor,
				"notifierService") {
			@Override
			protected VirtualMachine onExecute(VirtualMachine param, Void extra) {
				ArrayList<VirtualMachine> collection = new ArrayList<VirtualMachine>();
				collection.add(param);
				notifier.updateElements(collection);
				return param;
			}
		};
		this.networkClearingService = new BaseImediateCompletableFutureService<VirtualMachine, VirtualMachine, Void>(
				executor, "NetworkClearingService") {

			@Override
			protected VirtualMachine onExecute(VirtualMachine param, Void extra) {
				param.setHostname(null);
				param.setIpAddress(null);
				param.setInternalIpAddress(null);
				param.setInternalHostname(null);
				return param;
			}
		};
	}

	@Override
	public void addVmToProvisionPipeline(Collection<VirtualMachine> vms) {
		Void v = null;
		for (VirtualMachine vm : vms) {
			CompletableFuture<VirtualMachine> cf = awsRenamingService.startFutures(vm, v);
			cf = awsNetworkingUpdateService.chainFutures(cf, v);
			cf = ensureDeleteVolumeOnTermination.chainFutures(cf, v);
			cf = updateStatus.chainFutures(cf, VmState.LAUNCHING);
			cf = notifierService.chainFutures(cf, v);
			cf = testUpDown.chainFutures(cf, true);
			cf = addRsa.chainFutures(cf, v);
			cf = updateStatus.chainFutures(cf, VmState.RUNNING);
			cf = notifierService.chainFutures(cf, v);
		}

	}

	@Override
	public void addVmsToStartingPipeline(Collection<VirtualMachine> vms) {
		Void v = null;
		for (VirtualMachine vm : vms) {
			CompletableFuture<VirtualMachine> cf = awsNetworkingUpdateService.startFutures(vm, v);
			cf = notifierService.chainFutures(cf, v);
			cf = testUpDown.chainFutures(cf, true);
			cf = updateStatus.chainFutures(cf, VmState.RUNNING);
			cf = notifierService.chainFutures(cf, v);
		}
	}

	@Override
	public void addVmsToStoppingPipeline(Collection<VirtualMachine> vms) {
		Void v = null;
		for (VirtualMachine vm : vms) {
			CompletableFuture<VirtualMachine> cf = testUpDown.startFutures(vm, false);
			cf = networkClearingService.chainFutures(cf, v);
			cf = notifierService.chainFutures(cf, v);
			cf = awsUpdateStatus.chainFutures(cf, VmState.STOPPED);
			cf = notifierService.chainFutures(cf, v);
		}
	}

	@Override
	public void addVmsToDeletingPipeline(Collection<VirtualMachine> vms) {
		Void v = null;
		for (VirtualMachine vm : vms) {
			CompletableFuture<VirtualMachine> cf = testUpDown.startFutures(vm, false);
			cf = networkClearingService.chainFutures(cf, v);
			cf = notifierService.chainFutures(cf, v);
			cf = awsUpdateStatus.chainFutures(cf, VmState.DELETED);
			cf = notifierService.chainFutures(cf, v);
		}
	}

	public static void main(String[] args) throws InterruptedException, ExecutionException {

		CompletableFuture<Integer> source = CompletableFuture.completedFuture(1);
		CompletableFuture<Integer> cf1 = addOneService(source);
		CompletableFuture<Integer> cf2 = addOneService(cf1);
		CompletableFuture<Integer> cf3 = addOneService(cf2);
		CompletableFuture<Integer> cf4 = addOneService(cf3);
		JavaUtil.sleepAndLogInterruption(6000);
		logger.debug("" + cf1.get());
		logger.debug("" + cf2.get());
		logger.debug("" + cf3.get());
		logger.debug("" + cf4.get());

	}

	static Random rand = new Random();

	private static CompletableFuture<Integer> addOneServiceProvider(Integer val) {
		CompletableFuture<Integer> cf = new CompletableFuture<Integer>();
		new Thread(new Runnable() {

			@Override
			public void run() {
				JavaUtil.sleepAndLogInterruption(200 + rand.nextInt(1800));
				cf.complete(val + 1);
			}
		}).start();
		return cf;
	}

	private static CompletableFuture<Integer> addOneService(CompletableFuture<Integer> priorCf) {
		CompletableFuture<Integer> cf = new CompletableFuture<Integer>();
		priorCf.thenAcceptAsync(new Consumer<Integer>() {

			@Override
			public void accept(Integer t) {
				logger.debug("accepted: " + t);
				Runnable r = new Runnable() {

					@Override
					public void run() {
						JavaUtil.sleepAndLogInterruption(200 + rand.nextInt(1800));
						logger.debug("" + t);
						cf.complete(t + 1);
					}
				};
				new Thread(r).start();
			}
		});
		return cf;
	}

	private static CompletableFuture<Integer> minus2Service(CompletableFuture<Integer> priorCf) {
		CompletableFuture<Integer> cf = new CompletableFuture<Integer>();
		priorCf.thenAcceptAsync(new Consumer<Integer>() {

			@Override
			public void accept(Integer t) {
				logger.debug("accepted: " + t);
				Runnable r = new Runnable() {

					@Override
					public void run() {
						JavaUtil.sleepAndLogInterruption(200 + rand.nextInt(1800));
						logger.debug("" + t);
						cf.complete(t - 2);
					}
				};
				new Thread(r).start();
			}
		});

		return cf;
	}

}
