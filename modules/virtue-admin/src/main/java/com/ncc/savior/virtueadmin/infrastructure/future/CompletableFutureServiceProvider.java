package com.ncc.savior.virtueadmin.infrastructure.future;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.amazonaws.services.ec2.AmazonEC2;
import com.ncc.savior.virtueadmin.infrastructure.IKeyManager;
import com.ncc.savior.virtueadmin.infrastructure.IUpdateListener;
import com.ncc.savior.virtueadmin.infrastructure.aws.VirtueAwsEc2Provider;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VmState;

/**
 * Class that creates and stores all the Completable Future Services
 * ({@link BaseCompletableFutureService}). This is just a shortcut class to
 * store all the services. They could in the future be injected individually in
 * class that need them.
 * 
 *
 */
public class CompletableFutureServiceProvider {
	private static final Logger logger = LoggerFactory.getLogger(CompletableFutureServiceProvider.class);
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

	@Value("${virtue.services.timeout.updown:0}")
	private int upDownTimeoutMillis;
	@Value("${virtue.services.timeout.aws.rename:0}")
	private int renameTimeoutMillis;
	@Value("${virtue.services.timeout.aws.networking:0}")
	private int networkTimeoutMillis;
	@Value("${virtue.services.timeout.aws.ensureDelete:0}")
	private int ensureDeleteTimeoutMillis;
	@Value("${virtue.services.timeout.rsa:0}")
	private int rsaTimeoutMillis;
	@Value("${virtue.services.timeout.aws.status:0}")
	private int awsStatusTimeoutMillis;
	@Value("${virtue.sensing.redirectUrl}")
	private String sensingUri;
	@Value("${virtue.aws.persistentStorage.deviceName}")
	private String persistentVolumeDeviceName;
	private boolean usePublicDns;
	private VirtueAwsEc2Provider ec2Provider;
	private IKeyManager keyManager;
	private IUpdateListener<VirtualMachine> vmNotifier;
	private RunRemoteCommandCompletableFutureService runRemoteCommand;
	private RunRemoteScriptCompletableFutureService runRemoteScript;
	private WindowsAccountGenerator windowsAccountGenerator;

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
		this.usePublicDns = usePublicDns;
		this.ec2Provider = ec2Provider;
		this.keyManager = keyManager;
		this.vmNotifier = vmNotifier;
	}

	/**
	 * needs to be called after constructor due to timeouts. This is done
	 * automatically via Spring with the init-method attribute in the
	 * application-context.xml
	 */
	protected void init() {
		AmazonEC2 ec2 = ec2Provider.getEc2();
		awsRenamingService = new AwsRenamingCompletableFutureService(executor, ec2, renameTimeoutMillis);
		awsNetworkingUpdateService = new AwsNetworkingUpdateService(executor, ec2, usePublicDns, networkTimeoutMillis);
		ensureDeleteVolumeOnTermination = new EnsureDeleteVolumeOnTerminationCompletableFutureService(executor, ec2,
				ensureDeleteTimeoutMillis, persistentVolumeDeviceName);
		testUpDown = new TestReachabilityCompletableFuture(executor, keyManager, upDownTimeoutMillis);
		addRsa = new AddRsaKeyCompletableFutureService(executor, keyManager, rsaTimeoutMillis);
		awsUpdateStatus = new AwsUpdateStatusCompletableFutureService(executor, ec2, awsStatusTimeoutMillis);
		runRemoteCommand = new RunRemoteCommandCompletableFutureService(executor, keyManager);
		runRemoteScript = new RunRemoteScriptCompletableFutureService(executor, keyManager);
		windowsAccountGenerator = new WindowsAccountGenerator(executor, false, 10, 1000, 10000, keyManager);
		updateStatus = new BaseImediateCompletableFutureService<VirtualMachine, VirtualMachine, VmState>(
				"alterStatus") {
			@Override
			protected VirtualMachine onExecute(VirtualMachine param, VmState state) {
				param.setState(state);
				return param;
			}
		};
		vmNnotifierService = new BaseImediateCompletableFutureService<VirtualMachine, VirtualMachine, Void>(
				"notifierService") {
			@Override
			protected VirtualMachine onExecute(VirtualMachine param, Void extra) {
				logger.trace("SAVING: " + param.getState());
				ArrayList<VirtualMachine> collection = new ArrayList<VirtualMachine>();
				collection.add(param);
				vmNotifier.updateElements(collection);
				return param;
			}
		};
		this.networkClearingService = new BaseImediateCompletableFutureService<VirtualMachine, VirtualMachine, Void>(
				"NetworkClearingService") {

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
				"NetworkCopyingService") {

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
				"errorCausingService") {

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

	public RunRemoteCommandCompletableFutureService getRunRemoteCommand() {
		return runRemoteCommand;
	}

	public RunRemoteScriptCompletableFutureService getRunRemoteScript() {
		return runRemoteScript;
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

	public WindowsAccountGenerator getWindowsAccountGenerator() {
		return windowsAccountGenerator;
	}
}
