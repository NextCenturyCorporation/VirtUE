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
package com.ncc.savior.virtueadmin.infrastructure.windows;

import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.amazonaws.services.ec2.model.InstanceType;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.ncc.savior.util.Retrier;
import com.ncc.savior.util.SaviorErrorCode;
import com.ncc.savior.util.SaviorException;
import com.ncc.savior.util.SshUtil;
import com.ncc.savior.util.SshUtil.SshResult;
import com.ncc.savior.virtueadmin.infrastructure.IKeyManager;
import com.ncc.savior.virtueadmin.infrastructure.SimpleApplicationManager;
import com.ncc.savior.virtueadmin.infrastructure.aws.AwsEc2Wrapper;
import com.ncc.savior.virtueadmin.infrastructure.aws.AwsUtil;
import com.ncc.savior.virtueadmin.infrastructure.aws.AwsUtil.VirtuePrimaryPurpose;
import com.ncc.savior.virtueadmin.infrastructure.aws.AwsUtil.VirtueSecondaryPurpose;
import com.ncc.savior.virtueadmin.infrastructure.aws.VirtueCreationAdditionalParameters;
import com.ncc.savior.virtueadmin.infrastructure.aws.subnet.IVpcSubnetProvider;
import com.ncc.savior.virtueadmin.infrastructure.future.BaseImmediateCompletableFutureService;
import com.ncc.savior.virtueadmin.infrastructure.future.CompletableFutureServiceProvider;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.OS;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VirtualMachineTemplate;
import com.ncc.savior.virtueadmin.model.VirtueInstance;
import com.ncc.savior.virtueadmin.model.VmState;
import com.ncc.savior.virtueadmin.template.ITemplateService;
import com.ncc.savior.virtueadmin.template.ITemplateService.TemplateException;
import com.ncc.savior.virtueadmin.util.ServerIdProvider;

public class WindowsDisplayServerManager {
	private static final Logger logger = LoggerFactory.getLogger(WindowsDisplayServerManager.class);
	/**
	 * See com.ncc.savior.desktop.windows.WindowsApplicationLauncher in the
	 * clipboard module.
	 */
	private static final String APPLICATION_LAUNCH_FILE = "c:\\virtue\\app.txt";
	private static final String MODEL_KEY_APP_VM = "applicationVm";
	private static final String MODEL_KEY_DISPLAY_VM = "displayVm";
	private static final String MODEL_KEY_APPLICATION = "application";
	private static final String MODEL_KEY_PARAMS = "params";
	private static final String RDP_TEMPLATE_NAME = "xfreerdp-start-app.tpl";
	private static final String MODEL_KEY_WINDOWS_PASSWORD = "windowsPassword";
	private static final String MODEL_KEY_DISPLAY = "display";
	private CompletableFutureServiceProvider serviceProvider;
	private InstanceType instanceType;
	private String serverId;
	private AwsEc2Wrapper wrapper;
	private VirtualMachineTemplate windowsDisplayTemplate;
	private IVpcSubnetProvider vpcSubnetProvider;
	private IKeyManager keyManager;
	private ITemplateService templateService;
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
	@Value("${virtue.aws.windows.password}")
	private String windowsPassword;

	private BaseImmediateCompletableFutureService<VirtualMachine, VirtualMachine, Pair<String, String>> wdsVmUpdater;

	public WindowsDisplayServerManager(ServerIdProvider serverIdProvider, AwsEc2Wrapper awsEc2Wrapper,
			CompletableFutureServiceProvider serviceProvider, IVpcSubnetProvider vpcSubnetProvider,
			IKeyManager keyManager, ITemplateService templateService) {
		super();
		this.wrapper = awsEc2Wrapper;
		this.serviceProvider = serviceProvider;
		this.serverId = serverIdProvider.getServerId();
		this.vpcSubnetProvider = vpcSubnetProvider;
		this.keyManager = keyManager;
		this.templateService = templateService;

		this.wdsVmUpdater = new BaseImmediateCompletableFutureService<VirtualMachine, VirtualMachine, Pair<String, String>>(
				"WindowsDisplayServerUpdater") {

			@Override
			protected VirtualMachine onExecute(VirtualMachine param,
					Pair<String, String> usernameAndWindowsApplicationVmId) {
				String username = usernameAndWindowsApplicationVmId.getLeft();
				String windowsApplicationVmId = usernameAndWindowsApplicationVmId.getRight();
				wdsDao.updateDisplayServerVm(username, windowsApplicationVmId, param);
				logger.debug("Saving WindowsDisplayServer: " + windowsApplicationVmId + " " + param);
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
		cf = serviceProvider.getAddRsa().chainFutures(cf, v);
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
			if (dsvm != null) {
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
		List<String> windowsApplicationVmIds = windowsAppVms.stream().map(VirtualMachine::getId)
				.collect(Collectors.toList());
		Collection<VirtualMachine> displayVms = wdsDao
				.getDisplayServerVmsByWindowsApplicationVmIds(windowsApplicationVmIds);
		wrapper.deleteVirtualMachines(displayVms);
	}

	public VirtualMachine getWindowsDisplayVm(String windowsApplicationVmId) {
		return wdsDao.getDisplayServerVmByWindowsApplicationVmId(windowsApplicationVmId);
	}

	public void startApplication(VirtualMachine appVm, ApplicationDefinition application, String params, int retries) {
		if (!enabled) {
			return;
		}
		SimpleApplicationManager sam = new SimpleApplicationManager(templateService);
		String templateName = RDP_TEMPLATE_NAME;
		VirtualMachine displayVm = getWindowsDisplayVm(appVm.getId());

		File keyFile = keyManager.getKeyFileByName(displayVm.getPrivateKeyName());
		int display = sam.startOrGetXpraServerWithRetries(displayVm, keyFile, 15);
		if (params == null) {
			params = "";
		} else {
			params = " " + params;
		}

		try {
			Session session = SshUtil.getConnectedSession(displayVm, keyFile);
			Map<String, Object> dataModel = new HashMap<String, Object>();
			dataModel.put(MODEL_KEY_APP_VM, appVm);
			dataModel.put(MODEL_KEY_DISPLAY_VM, displayVm);
			dataModel.put(MODEL_KEY_APPLICATION, application);
			dataModel.put(MODEL_KEY_PARAMS, params);
			dataModel.put(MODEL_KEY_WINDOWS_PASSWORD, windowsPassword);
			dataModel.put(MODEL_KEY_DISPLAY, display);
			// create runnable because connection must remain open
			Runnable con = new Retrier(() -> {
				try {
					Session winSession = SshUtil.getConnectedSession(appVm,
							keyManager.getKeyFileByName(appVm.getPrivateKeyName()));
					List<String> out = SshUtil.sendCommandFromSession(winSession,
							"echo " + application.getLaunchCommand() + " > " + APPLICATION_LAUNCH_FILE);
					logger.debug("app out: {}", out);
				} catch (JSchException | IOException e) {
					throw new SaviorException(SaviorErrorCode.SSH_ERROR,
							"Error connecting to windows application VM to configure application. AppVm=" + appVm
									+ " application=" + application,
							e);
				}
				try {
					SshResult result = SshUtil.runTemplateFile(templateService, session, templateName, dataModel, 5000);

					int exitStatus = result.getExitStatus();
					if (exitStatus == -1) {
						logger.debug("letting app run in the background");
					} else if (exitStatus == 131) {
						throw new SaviorException(SaviorErrorCode.APPLICATION_NOT_FOUND,
								"Error starting application, it may not be installed. DisplayVm=" + displayVm
										+ " AppVm=" + appVm + " application=" + application);
					} else if (exitStatus != 0) {
						throw new SaviorException(SaviorErrorCode.UNKNOWN_ERROR,
								"Error " + exitStatus + " starting application. DisplayVm=" + displayVm + " AppVm="
										+ appVm + " application=" + application);
					}
					logger.debug("returned!**" + result.getOutput() + result.getError());
				} catch (InterruptedIOException e) {
					logger.debug("letting app run in the background");
				} catch (JSchException | IOException e) {
					throw new SaviorException(SaviorErrorCode.SSH_ERROR,
							"Error connecting to windows display VM to start application. DisplayVm=" + displayVm
									+ " AppVm=" + appVm + " application=" + application,
							e);
				} catch (TemplateException e) {
					throw new SaviorException(SaviorErrorCode.TEMPLATE_ERROR,
							"Error creating template. Template=" + templateName + " model=" + dataModel, e);
				}
			}, 5, 100, Collections.singleton(Throwable.class));
			Thread t = new Thread(con, "WindowsApplication");
			t.setDaemon(true);
			t.start();
		} catch (JSchException e) {
			throw new SaviorException(SaviorErrorCode.SSH_ERROR,
					"Error connection to windows display VM to start application. DisplayVm=" + displayVm + " AppVm="
							+ appVm + " application=" + application,
					e);
		}
	}

}
