package com.ncc.savior.virtueadmin.infrastructure.mixed;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.virtueadmin.infrastructure.ICloudManager;
import com.ncc.savior.virtueadmin.infrastructure.aws.AsyncAwsEc2VmManager;
import com.ncc.savior.virtueadmin.infrastructure.future.CompletableFutureServiceProvider;
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
 *
 */
public class XenAwsMixCloudManager implements ICloudManager {
	private static final Logger logger = LoggerFactory.getLogger(XenAwsMixCloudManager.class);

	private XenHostManager xenHostManager;
	private AsyncAwsEc2VmManager awsVmManager;

	private WindowsStartupAppsService windowsNfsMountingService;

	private CompletableFutureServiceProvider serviceProvider;

	public XenAwsMixCloudManager(XenHostManager xenHostManager, AsyncAwsEc2VmManager awsVmManager,
			CompletableFutureServiceProvider serviceProvider, WindowsStartupAppsService windowsNfsMountingService) {
		super();
		this.xenHostManager = xenHostManager;
		this.awsVmManager = awsVmManager;
		this.serviceProvider = serviceProvider;
		// TODO this is a little out of place, but will work here for now.
		this.windowsNfsMountingService = windowsNfsMountingService;
	}

	@Override
	public void deleteVirtue(VirtueInstance virtueInstance) {
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
		awsVmManager.deleteVirtualMachines(windowsVms, null);
		xenHostManager.deleteVirtue(virtueInstance.getId(), linuxVms);
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
		Collection<VirtualMachine> vms = awsVmManager.provisionVirtualMachineTemplates(user, windowsVmts,
				windowsFuture);
		VirtueInstance vi = new VirtueInstance(template, user.getUsername(), vms);
		// if (!linuxVmts.isEmpty()) {

		CompletableFuture<Collection<VirtualMachine>> linuxFuture = new CompletableFuture<Collection<VirtualMachine>>();
		CompletableFuture<VirtualMachine> xenFuture = new CompletableFuture<VirtualMachine>();
		// actually provisions xen host and then xen guests.
		xenHostManager.provisionXenHost(vi, linuxVmts, xenFuture, linuxFuture);
		// }
		windowsFuture.thenCombine(xenFuture, (Collection<VirtualMachine> winVms, VirtualMachine xen) -> {
			// windowsNfsMountingService.addVirtueToQueue(vi);
			for (VirtualMachine windows : winVms) {
				windowsNfsMountingService.addWindowsStartupServices(xen, windows);
			}
			return winVms;
		}).thenAccept((Collection<VirtualMachine> finishedWindowsBoxes) -> {
			for (VirtualMachine winBox : finishedWindowsBoxes) {
				CompletableFuture<VirtualMachine> myCf = serviceProvider.getUpdateStatus().startFutures(winBox,
						VmState.RUNNING);
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
}
