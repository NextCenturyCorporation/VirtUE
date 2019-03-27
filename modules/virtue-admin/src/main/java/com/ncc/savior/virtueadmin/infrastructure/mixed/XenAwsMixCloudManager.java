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
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;

import com.ncc.savior.util.SaviorErrorCode;
import com.ncc.savior.util.SaviorException;
import com.ncc.savior.virtueadmin.cifsproxy.CifsManager;
import com.ncc.savior.virtueadmin.infrastructure.ICloudManager;
import com.ncc.savior.virtueadmin.infrastructure.aws.AsyncAwsEc2VmManager;
import com.ncc.savior.virtueadmin.infrastructure.aws.VirtueCreationAdditionalParameters;
import com.ncc.savior.virtueadmin.infrastructure.aws.securitygroups.ISecurityGroupManager;
import com.ncc.savior.virtueadmin.infrastructure.aws.subnet.IVpcSubnetProvider;
import com.ncc.savior.virtueadmin.infrastructure.future.CompletableFutureServiceProvider;
import com.ncc.savior.virtueadmin.infrastructure.windows.WindowsDisplayServerManager;
import com.ncc.savior.virtueadmin.model.FileSystem;
import com.ncc.savior.virtueadmin.model.OS;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VirtualMachineTemplate;
import com.ncc.savior.virtueadmin.model.VirtueInstance;
import com.ncc.savior.virtueadmin.model.VirtueTemplate;
import com.ncc.savior.virtueadmin.model.VirtueUser;
import com.ncc.savior.virtueadmin.model.VmState;

/**
 * Cloud manager that mixes AWS instances for Windows VMs via an
 * {@link AsyncAwsEc2VmManager}, but defers any linux boxes to a
 * {@link XenHostManager}.
 * 
 * This implementation is based on {@link CompletableFuture}s.
 * {@link CompletableFuture}s allow for asynchronous tasks to be chained
 * together to occur in order when the previous completes. With access to the
 * future, code has the ability to complete it successfully or exceptionally at
 * any time.
 * 
 * <ul>
 * <li>TODO Errors usually get passed through the chain, but are often not
 * handled here. Need to handle errors better
 * <li>TODO Need to interact with futures from outside events. (I.E. VM could be
 * deleted on AWS while the system thinks it is still provisioning)
 * <li>TODO There are no timeouts for services where they may never complete
 * (reachability for a VM that has been deleted)
 * <li>Nothing prevents a Virtue that is in one process from being acted upon.
 * I.E. if a virtue is provisioning, it can be requested to be deleted. Doing
 * this would cause issues where futures would get lost and status may not be
 * accurate. We should allow for the current future of a VM to be retrieved and
 * interrupted (completeExceptionally).
 * 
 *
 */
public class XenAwsMixCloudManager implements ICloudManager {
	private static final Logger logger = LoggerFactory.getLogger(XenAwsMixCloudManager.class);

	private XenHostManager xenHostManager;
	private AsyncAwsEc2VmManager awsVmManager;

	private WindowsStartupAppsService windowsNfsMountingService;

	private CompletableFutureServiceProvider serviceProvider;

	private IVpcSubnetProvider vpcSubnetProvider;

	private ISecurityGroupManager securityGroupManager;

	private CifsManager cifsManager;

	private WindowsDisplayServerManager windowsDisplayManager;

	public XenAwsMixCloudManager(XenHostManager xenHostManager, AsyncAwsEc2VmManager awsVmManager,
			CompletableFutureServiceProvider serviceProvider, WindowsStartupAppsService windowsNfsMountingService,
			IVpcSubnetProvider vpcSubnetProvider, ISecurityGroupManager securityGroupManager,
			WindowsDisplayServerManager windowsDisplayManager) {
		super();
		this.xenHostManager = xenHostManager;
		this.awsVmManager = awsVmManager;
		this.serviceProvider = serviceProvider;
		this.vpcSubnetProvider = vpcSubnetProvider;
		this.securityGroupManager = securityGroupManager;
		// TODO this is a little out of place, but will work here for now.
		this.windowsNfsMountingService = windowsNfsMountingService;
		this.windowsDisplayManager = windowsDisplayManager;
	}

	@Override
	public void deleteVirtue(VirtueInstance virtueInstance, CompletableFuture<VirtueInstance> future) {
		Collection<VirtualMachine> vms = virtueInstance.getVms();
		Collection<VirtualMachine> linuxVms = new ArrayList<VirtualMachine>();
		Collection<VirtualMachine> windowsVms = new ArrayList<VirtualMachine>();
		for (VirtualMachine vm : vms) {
			if (OS.LINUX.equals(vm.getOs())) {
				linuxVms.add(vm);
			} else if (OS.WINDOWS.equals(vm.getOs())) {
				windowsVms.add(vm);
			}
		}
		CompletableFuture<Collection<VirtualMachine>> windowsFuture = new CompletableFuture<Collection<VirtualMachine>>();
		CompletableFuture<VirtualMachine> xenFuture = new CompletableFuture<VirtualMachine>();
		cifsManager.cifsBeforeVirtueDelete(virtueInstance);
		awsVmManager.deleteVirtualMachines(windowsVms, windowsFuture);
		windowsDisplayManager.deleteWindowsDisplay(windowsVms);
		xenHostManager.deleteVirtue(virtueInstance.getId(), linuxVms, xenFuture, null);
		// Database needs to be updated when we delete, but we don't have access here.
		// This is one of a couple ways to handle this and could be reviewed. Most
		// likely, the notifiers (that usually update the database in the
		// CompletableFuture services) should have delete/remove methods.
		//
		// Currently, we use this future to pass it out so we can delete it elsewhere.
		CompletableFuture.allOf(windowsFuture, xenFuture).thenRun(() -> {
			logger.debug("Attempting to release subnet for " + virtueInstance.getName());
			vpcSubnetProvider.releaseBySubnetKey(virtueInstance.getId());
			future.complete(virtueInstance);
		});
	}

	@Override
	public VirtueInstance createVirtue(VirtueUser user, VirtueTemplate template) throws Exception {
		logger.debug("creating virtue from template=" + template.getId());
		Collection<VirtualMachineTemplate> linuxVmts = new ArrayList<VirtualMachineTemplate>();
		Collection<VirtualMachineTemplate> windowsVmts = new ArrayList<VirtualMachineTemplate>();
		Collection<VirtualMachineTemplate> vmts = template.getVmTemplates();
		for (VirtualMachineTemplate vmt : vmts) {
			if (OS.LINUX.equals(vmt.getOs())) {
				linuxVmts.add(vmt);
			} else if (OS.WINDOWS.equals(vmt.getOs())) {
				windowsVmts.add(vmt);
			}
		}

		CompletableFuture<Collection<VirtualMachine>> windowsFuture = new CompletableFuture<Collection<VirtualMachine>>();
		VirtueInstance vi = new VirtueInstance(template, user.getUsername(), null);

		String virtueSecurityGroupId = securityGroupManager.getSecurityGroupIdByTemplateId(vi.getTemplateId());
		VirtueCreationAdditionalParameters virtueMods = new VirtueCreationAdditionalParameters(template.getName());
		// virtueMods.setSubnetId(subnetId);
		virtueMods.setSecurityGroupId(virtueSecurityGroupId);
		virtueMods.setVirtueId(vi.getId());
		virtueMods.setVirtueTemplateId(vi.getTemplateId());

		CompletableFuture<Collection<VirtualMachine>> linuxFuture = new CompletableFuture<Collection<VirtualMachine>>();
		CompletableFuture<VirtualMachine> xenFuture = new CompletableFuture<VirtualMachine>();
		// actually provisions xen host and then xen guests.
		// Xen must set subnet id
		vi.setVms(new ArrayList<VirtualMachine>());
		xenHostManager.provisionXenHost(vi, linuxVmts, xenFuture, linuxFuture, virtueMods);

		Collection<VirtualMachine> windowsVms = awsVmManager.provisionVirtualMachineTemplates(user, windowsVmts,
				windowsFuture, virtueMods);
		vi.getVms().addAll(windowsVms);

		for (VirtualMachine windowsVm : windowsVms) {
			windowsDisplayManager.setupWindowsDisplay(vi, windowsVm, virtueMods.getSubnetId());
		}

		// if (!linuxVmts.isEmpty()) {

		// Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		Collection<FileSystem> fileSystems = template.getFileSystems();
		String password = (String) SecurityContextHolder.getContext().getAuthentication().getCredentials();
		try {
			Future<Exception> cifsPriorTask = cifsManager.cifsBeforeVirtueCreation(vi, fileSystems);
			linuxFuture.thenAccept((myLinuxVms) -> {
				// Authentication auth2 =
				// SecurityContextHolder.getContext().getAuthentication();
				try {
					Exception e = cifsPriorTask.get();
					if (e == null) {
						cifsManager.addFilesystemToVms(vi, user, fileSystems, myLinuxVms, password);
					} else {
						throw new SaviorException(SaviorErrorCode.CIFS_PROXY_ERROR, "Cifs Prior Task failed!", e);
					}
				} catch (Throwable t) {
					// TODO need to fix how we handle this error.
					logger.error("error creating cifs", t);
				}
			});
		} catch (SaviorException e) {
			// TODO we probably dont want to ignore
			logger.error("Error with CIFS proxy.  Ignoring and continuing!", e);
		}
		// }
		windowsFuture.thenCombine(xenFuture, (Collection<VirtualMachine> winVms, VirtualMachine xen) -> {
			// When xen (really NFS) and all windows VM's are up
			// Add them to the windows startup services service

			// windowsNfsMountingService.addVirtueToQueue(vi);
			logger.debug("Adding vms to Windows Startup Services");
			for (VirtualMachine windows : winVms) {
				try {
					windowsNfsMountingService.addWindowsStartupServices(xen, windows);
				} catch (Throwable t) {
					logger.error("error adding to windows services", t);
				}
			}
			logger.debug("Finished adding vms to Windows Startup Services");
			return winVms;
		}).thenApply((Collection<VirtualMachine> finishedWindowsBoxes) -> {
			try {
				cifsManager.addFilesystemToVms(vi, user, fileSystems, finishedWindowsBoxes, password);
			} catch (Throwable t) {
				// TODO need to fix how we handle this error.
				logger.error("error creating cifs", t);
			}
			return finishedWindowsBoxes;
		}).thenAccept((Collection<VirtualMachine> finishedWindowsBoxes) -> {
			// Once startup services are done on windows machines, set VM state to running
			// and notify (notify saves to DB typically)
			logger.debug("finished with windows boxes.  Setting to running vms=" + finishedWindowsBoxes);
			for (VirtualMachine winBox : finishedWindowsBoxes) {
				VmState state = VmState.RUNNING;
				try {
					windowsDisplayManager.waitForDisplayServerRunning(winBox.getId());
				} catch (SaviorException e) {
					logger.error("Error setting up windows display server for VM=" + winBox, e);
					state = VmState.ERROR;
				}
				CompletableFuture<VirtualMachine> myCf = serviceProvider.getUpdateStatus().startFutures(winBox, state);
				serviceProvider.getVmNotifierService().chainFutures(myCf, null);
			}
		});

		return vi;
	}

	@Override
	public VirtueInstance startVirtue(VirtueInstance virtueInstance) {
		Collection<VirtualMachine> vms = virtueInstance.getVms();
		Collection<VirtualMachine> linuxVms = new ArrayList<VirtualMachine>();
		Collection<VirtualMachine> windowsVms = new ArrayList<VirtualMachine>();
		for (VirtualMachine vm : vms) {
			if (OS.LINUX.equals(vm.getOs())) {
				linuxVms.add(vm);
			} else if (OS.WINDOWS.equals(vm.getOs())) {
				windowsVms.add(vm);
			}
		}
		xenHostManager.startVirtue(virtueInstance, linuxVms, null, null);
		awsVmManager.startVirtualMachines(windowsVms, null);
		return virtueInstance;
	}

	@Override
	public VirtueInstance stopVirtue(VirtueInstance virtueInstance) {
		Collection<VirtualMachine> vms = virtueInstance.getVms();
		Collection<VirtualMachine> linuxVms = new ArrayList<VirtualMachine>();
		Collection<VirtualMachine> windowsVms = new ArrayList<VirtualMachine>();
		for (VirtualMachine vm : vms) {
			if (OS.LINUX.equals(vm.getOs())) {
				linuxVms.add(vm);
			} else if (OS.WINDOWS.equals(vm.getOs())) {
				windowsVms.add(vm);
			}
		}
		xenHostManager.stopVirtue(virtueInstance, linuxVms, null, null);
		awsVmManager.stopVirtualMachines(windowsVms, null);
		return virtueInstance;
	}

	@Override
	public void rebootVm(VirtualMachine vm, String virtue) {
		if (OS.LINUX.equals(vm.getOs())) {
			XenGuestManager guestManager = xenHostManager.getGuestManager(virtue);
			guestManager.rebootVm(vm, null);
		} else if (OS.WINDOWS.equals(vm.getOs())) {
			awsVmManager.rebootVm(vm, null);
		}
	}

	@Override
	public void sync(List<String> ids) {
		awsVmManager.syncAll(ids);
	}

	@Override
	public void setCifsManager(CifsManager cifsManager) {
		this.cifsManager = cifsManager;
	}
}
