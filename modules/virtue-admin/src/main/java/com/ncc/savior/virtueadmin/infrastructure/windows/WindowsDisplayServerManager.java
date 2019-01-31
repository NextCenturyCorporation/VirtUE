package com.ncc.savior.virtueadmin.infrastructure.windows;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.amazonaws.services.ec2.model.InstanceType;
import com.ncc.savior.util.SaviorErrorCode;
import com.ncc.savior.util.SaviorException;
import com.ncc.savior.virtueadmin.infrastructure.aws.AwsEc2Wrapper;
import com.ncc.savior.virtueadmin.infrastructure.aws.AwsUtil;
import com.ncc.savior.virtueadmin.infrastructure.aws.AwsUtil.VirtuePrimaryPurpose;
import com.ncc.savior.virtueadmin.infrastructure.aws.AwsUtil.VirtueSecondaryPurpose;
import com.ncc.savior.virtueadmin.infrastructure.aws.VirtueCreationAdditionalParameters;
import com.ncc.savior.virtueadmin.infrastructure.aws.subnet.IVpcSubnetProvider;
import com.ncc.savior.virtueadmin.infrastructure.future.BaseImediateCompletableFutureService;
import com.ncc.savior.virtueadmin.infrastructure.future.CompletableFutureServiceProvider;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.OS;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VirtualMachineTemplate;
import com.ncc.savior.virtueadmin.model.VirtueInstance;
import com.ncc.savior.virtueadmin.model.VmState;
import com.ncc.savior.virtueadmin.util.ServerIdProvider;

public class WindowsDisplayServerManager {
	private static final Logger logger = LoggerFactory.getLogger(WindowsDisplayServerManager.class);
	private CompletableFutureServiceProvider serviceProvider;
	private InstanceType instanceType;
	private String serverId;
	private AwsEc2Wrapper wrapper;
	private VirtualMachineTemplate windowsDisplayTemplate;
	private IVpcSubnetProvider vpcSubnetProvider;
	private Collection<String> securityGroupIds;
	@Autowired
	private IWindowsDisplayServerDao wdsDao;

	@Value("${virtue.winDisplay.enabled}")
	private boolean enabled;
	@Value("${virtue.winDisplay.aws.ami}")
	private String windowsDisplayAmi;
	@Value("${virtue.winDisplay.loginuser}")
	private String windowsDisplayLoginUser;
	@Value("${virtue.winDisplay.privateKeyName}")
	private String windowsDisplayPrivateKeyName;
	@Value("${virtue.winDisplay.securityGroups}")
	private String securityGroupsCommaSeparated;
	@Value("${virtue.winDisplay.timeoutMillis}")
	private long displayServerTimeoutMillis;
	@Value("${virtue.winDisplay.instanceType}")
	private String instanceTypeString;

	private BaseImediateCompletableFutureService<VirtualMachine, VirtualMachine, Pair<String, String>> wdsVmUpdater;

	public WindowsDisplayServerManager(ServerIdProvider serverIdProvider, AwsEc2Wrapper awsEc2Wrapper,
			CompletableFutureServiceProvider serviceProvider, IVpcSubnetProvider vpcSubnetProvider) {
		super();
		this.wrapper = awsEc2Wrapper;
		this.serviceProvider = serviceProvider;
		this.serverId = serverIdProvider.getServerId();
		this.vpcSubnetProvider = vpcSubnetProvider;

		this.wdsVmUpdater = new BaseImediateCompletableFutureService<VirtualMachine, VirtualMachine, Pair<String, String>>(
				"WindowsDisplayServerUpdater") {

			@Override
			protected VirtualMachine onExecute(VirtualMachine param,
					Pair<String, String> usernameAndWindowsApplicationVmId) {
				String username = usernameAndWindowsApplicationVmId.getLeft();
				String windowsApplicationVmId = usernameAndWindowsApplicationVmId.getRight();
				wdsDao.updateDisplayServerVm(username, windowsApplicationVmId, param);
				logger.debug("Saving WindowsDisplayServer: "+windowsApplicationVmId +" "+ param);
				return param;
			}
		};
	}

	protected void init() {
		this.instanceType = InstanceType.fromValue(instanceTypeString);
		String vpcId = vpcSubnetProvider.getVpcId();
		this.securityGroupIds = AwsUtil.getSecurityGroupIdsByNameAndVpcId(splitOnComma(securityGroupsCommaSeparated),
				vpcId, wrapper);
		this.windowsDisplayTemplate = new VirtualMachineTemplate(UUID.randomUUID().toString(),
				"WindowsDisplayServerTemplate", OS.LINUX, windowsDisplayAmi, new ArrayList<ApplicationDefinition>(),
				windowsDisplayLoginUser, true, new Date(0), "system");
	}

	private static Collection<String> splitOnComma(String securityGroupsCommaSeparated) {
		Collection<String> groups = new ArrayList<String>();
		if (securityGroupsCommaSeparated != null) {
			for (String group : securityGroupsCommaSeparated.split(",")) {
				groups.add(group.trim());
			}
		}
		return groups;
	}

	public VirtualMachine setupWindowsDisplay(VirtueInstance vi, VirtualMachine windowsVm, String subnetId) {
		if (enabled) {
			logger.debug("Creating windows display proxy for user=" + vi.getUsername());
			String name = "VRTU-WinDis-" + serverId + "-" + windowsVm.getName();
			VirtueCreationAdditionalParameters virtueMods = new VirtueCreationAdditionalParameters(name);
			virtueMods.setPrimaryPurpose(VirtuePrimaryPurpose.WINDOWS_DISPLAY);
			virtueMods.setSecondaryPurpose(VirtueSecondaryPurpose.WINDOWS_DISPLAY);
			virtueMods.setUsername(vi.getUsername());
			virtueMods.setVirtueId(vi.getId());
			virtueMods.setVirtueTemplateId(vi.getTemplateId());
			virtueMods.setSubnetId(subnetId);
			VirtualMachine displayVm = wrapper.provisionVm(this.windowsDisplayTemplate, name, securityGroupIds,
					windowsDisplayPrivateKeyName, instanceType, virtueMods, null);
			addVmToProvisionPipeline(displayVm, vi.getUsername(), windowsVm.getId(),
					new CompletableFuture<VirtualMachine>());
			return displayVm;
		} else {
			return windowsVm;
		}
	}

	private void addVmToProvisionPipeline(VirtualMachine vm, String username, String appVmId,
			CompletableFuture<VirtualMachine> future) {
		Void v = null;
		Pair<String, String> usernameAndAppVmId = Pair.of(username, appVmId);
		CompletableFuture<VirtualMachine> cf = serviceProvider.getAwsRenamingService().startFutures(vm, v);
		cf = serviceProvider.getAwsNetworkingUpdateService().chainFutures(cf, v);
		cf = serviceProvider.getEnsureDeleteVolumeOnTermination().chainFutures(cf, v);
		cf = wdsVmUpdater.chainFutures(cf, usernameAndAppVmId);
		// cf = serviceProvider.getUpdateStatus().chainFutures(cf, VmState.LAUNCHING);
		// cf = serviceProvider.getVmNotifierService().chainFutures(cf, v);
		cf = serviceProvider.getTestUpDown().chainFutures(cf, true);
		// cf = addScriptsToRunLaterTemplated(vm, cf);
		// cf = addScriptsToRunLaterOld(vm, cf);
		cf = serviceProvider.getUpdateStatus().chainFutures(cf, VmState.RUNNING);
		cf = wdsVmUpdater.chainFutures(cf, usernameAndAppVmId);
		cf.thenAccept((VirtualMachine myVm) -> {
			logger.debug("WinDisSer Proxy future complete.  Vm=" + myVm);
			future.complete(myVm);
		});
		cf.exceptionally((ex) -> {
			logger.error("EXCEPTION", ex);
			future.completeExceptionally(ex);
			vm.setState(VmState.ERROR);
			return vm;
		});
	}

	public void waitForDisplayServerRunning(String winAppVmId) {
		if (!enabled) {
			return;
		}
		long timeoutTime = System.currentTimeMillis() + displayServerTimeoutMillis;

		// breaking out of while is error path, either by timeout or some other unknown
		// error.
		// returning is correct path
		while (System.currentTimeMillis() <= timeoutTime) {
			VirtualMachine dsvm = wdsDao.getDisplayServerVmByWindowsApplicationVmId(winAppVmId);
			if (dsvm != null ) {
				logger.debug("test");
				VmState state = dsvm.getState();
				if (VmState.RUNNING.equals(state)) {
					return;
				}
				if (VmState.ERROR.equals(state)) {
					break;
				}
			}
		}
		// error or timeout occured and we broke out of while
		throw new SaviorException(SaviorErrorCode.SERVICE_TIMEOUT, "Windows Display Server timeout");
	}

	public void deleteWindowsDisplay(Collection<VirtualMachine> windowsAppVms) {
		if (!enabled) {
			return;
		}
		List<String> windowsApplicationVmIds = windowsAppVms.stream().map(VirtualMachine::getId).collect(Collectors.toList());
		Collection<VirtualMachine> displayVms = wdsDao.getDisplayServerVmsByWindowsApplicationVmIds(windowsApplicationVmIds);
		wrapper.deleteVirtualMachines(displayVms);
	}

}
