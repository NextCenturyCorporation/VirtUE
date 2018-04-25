package com.ncc.savior.virtueadmin.infrastructure.mixed;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.virtueadmin.infrastructure.ICloudManager;
import com.ncc.savior.virtueadmin.infrastructure.aws.AsyncAwsEc2VmManager;
import com.ncc.savior.virtueadmin.model.OS;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VirtualMachineTemplate;
import com.ncc.savior.virtueadmin.model.VirtueInstance;
import com.ncc.savior.virtueadmin.model.VirtueTemplate;
import com.ncc.savior.virtueadmin.model.VirtueUser;

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

	public XenAwsMixCloudManager(XenHostManager xenHostManager, AsyncAwsEc2VmManager awsVmManager,
			WindowsStartupAppsService windowsNfsMountingService) {
		super();
		this.xenHostManager = xenHostManager;
		this.awsVmManager = awsVmManager;
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
		awsVmManager.deleteVirtualMachines(windowsVms);
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

		// actually provisions xen host and then xen guests.
		xenHostManager.provisionXenHost(vi, linuxVmts);
		// }

		windowsNfsMountingService.addVirtueToQueue(vi);

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
		xenHostManager.startVirtue(virtueInstance, linuxVms);
		awsVmManager.startVirtualMachines(windowsVms);
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
		xenHostManager.stopVirtue(virtueInstance, linuxVms);
		awsVmManager.stopVirtualMachines(windowsVms);
		return virtueInstance;
	}
}
