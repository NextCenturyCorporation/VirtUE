package com.ncc.savior.virtueadmin.infrastructure.future;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

import com.amazonaws.services.ec2.AmazonEC2;
import com.ncc.savior.virtueadmin.infrastructure.IKeyManager;
import com.ncc.savior.virtueadmin.infrastructure.IUpdateListener;
import com.ncc.savior.virtueadmin.infrastructure.aws.VirtueAwsEc2Provider;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VmState;

public class CompletableFutureServiceProvider {
	private AwsRenamingCompletableFutureService awsRenamingService;
	private AwsNetworkingUpdateService awsNetworkingUpdateService;
	private EnsureDeleteVolumeOnTerminationCompletableFutureService ensureDeleteVolumeOnTermination;
	private TestReachabilityCompletableFuture testUpDown;
	private AddRsaKeyCompletableFutureService addRsa;
	private BaseImediateCompletableFutureService<VirtualMachine, VirtualMachine, VmState> updateStatus;
	private BaseImediateCompletableFutureService<VirtualMachine, VirtualMachine, Void> vmNnotifierService;
	private BaseImediateCompletableFutureService<VirtualMachine, VirtualMachine, Void> networkClearingService;
	private AwsUpdateStatusCompletableFutureService awsUpdateStatus;
	private ScheduledExecutorService executor;
	private BaseCompletableFutureService<VirtualMachine, VirtualMachine, VirtualMachine> networkCopyingService;
	private BaseCompletableFutureService<VirtualMachine, VirtualMachine, Void> errorCausingService;

	public CompletableFutureServiceProvider(VirtueAwsEc2Provider ec2Provider,
			IUpdateListener<VirtualMachine> vmNotifier, IKeyManager keyManager, boolean usePublicDns) {
		this.executor = Executors.newScheduledThreadPool(6, new ThreadFactory() {
			private int num = 1;

			@Override
			public synchronized Thread newThread(Runnable r) {
				String name = "future-service-runner" + num;
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
		vmNnotifierService = new BaseImediateCompletableFutureService<VirtualMachine, VirtualMachine, Void>(executor,
				"notifierService") {
			@Override
			protected VirtualMachine onExecute(VirtualMachine param, Void extra) {
				ArrayList<VirtualMachine> collection = new ArrayList<VirtualMachine>();
				collection.add(param);
				vmNotifier.updateElements(collection);
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
		this.networkCopyingService = new BaseImediateCompletableFutureService<VirtualMachine, VirtualMachine, VirtualMachine>(
				executor, "NetworkCopyingService") {

			@Override
			protected VirtualMachine onExecute(VirtualMachine param, VirtualMachine extra) {
				param.setHostname(extra.getHostname());
				param.setIpAddress(extra.getIpAddress());
				param.setInternalIpAddress(extra.getInternalIpAddress());
				param.setInternalHostname(extra.getInternalHostname());
				return param;
			}
		};
		this.errorCausingService = new BaseImediateCompletableFutureService<VirtualMachine, VirtualMachine, Void>(
				executor, "errorCausingService") {

			@Override
			protected VirtualMachine onExecute(VirtualMachine param, Void extra) {
				throw new RuntimeException("Forced Exception");
			}
		};
	}

	public AwsRenamingCompletableFutureService getAwsRenamingService() {
		return awsRenamingService;
	}

	public AwsNetworkingUpdateService getAwsNetworkingUpdateService() {
		return awsNetworkingUpdateService;
	}

	public EnsureDeleteVolumeOnTerminationCompletableFutureService getEnsureDeleteVolumeOnTermination() {
		return ensureDeleteVolumeOnTermination;
	}

	public TestReachabilityCompletableFuture getTestUpDown() {
		return testUpDown;
	}

	public AddRsaKeyCompletableFutureService getAddRsa() {
		return addRsa;
	}

	public BaseImediateCompletableFutureService<VirtualMachine, VirtualMachine, VmState> getUpdateStatus() {
		return updateStatus;
	}

	public BaseImediateCompletableFutureService<VirtualMachine, VirtualMachine, Void> getVmNotifierService() {
		return vmNnotifierService;
	}

	public BaseImediateCompletableFutureService<VirtualMachine, VirtualMachine, Void> getNetworkClearingService() {
		return networkClearingService;
	}

	public AwsUpdateStatusCompletableFutureService getAwsUpdateStatus() {
		return awsUpdateStatus;
	}

	public ScheduledExecutorService getExecutor() {
		return executor;
	}

	public BaseCompletableFutureService<VirtualMachine, VirtualMachine, VirtualMachine> getNetworkSettingService() {
		return networkCopyingService;
	}

	public BaseCompletableFutureService<VirtualMachine, VirtualMachine, Void> getErrorCausingService() {
		return errorCausingService;
	}
}
