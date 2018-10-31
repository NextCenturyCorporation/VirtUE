package com.ncc.savior.virtueadmin.infrastructure.mixed;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.virtueadmin.infrastructure.ICloudManager;
import com.ncc.savior.virtueadmin.infrastructure.aws.AsyncAwsEc2VmManager;
import com.ncc.savior.virtueadmin.infrastructure.aws.AwsUtil;
import com.ncc.savior.virtueadmin.infrastructure.aws.VirtueCreationAdditionalParameters;
import com.ncc.savior.virtueadmin.infrastructure.aws.securitygroups.ISecurityGroupManager;
import com.ncc.savior.virtueadmin.infrastructure.aws.subnet.IVpcSubnetProvider;
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

	public XenAwsMixCloudManager(XenHostManager xenHostManager, AsyncAwsEc2VmManager awsVmManager,
			CompletableFutureServiceProvider serviceProvider, WindowsStartupAppsService windowsNfsMountingService,
			IVpcSubnetProvider vpcSubnetProvider, ISecurityGroupManager securityGroupManager) {
		super();
		this.xenHostManager = xenHostManager;
		this.awsVmManager = awsVmManager;
		this.serviceProvider = serviceProvider;
		this.vpcSubnetProvider = vpcSubnetProvider;
		this.securityGroupManager = securityGroupManager;
		// TODO this is a little out of place, but will work here for now.
		this.windowsNfsMountingService = windowsNfsMountingService;
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
		awsVmManager.deleteVirtualMachines(windowsVms, windowsFuture);
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
		Map<String, String> tags = new HashMap<String, String>();
		tags.put(AwsUtil.TAG_USERNAME, user.getUsername());
		tags.put(AwsUtil.TAG_VIRTUE_NAME, vi.getName());
		tags.put(AwsUtil.TAG_INSTANCE_ID, vi.getId());
		String subnetId = vpcSubnetProvider.getSubnetId(vi.getId(), tags);
		String virtueSecurityGroupId = securityGroupManager.getSecurityGroupIdByTemplateId(vi.getTemplateId());
		VirtueCreationAdditionalParameters virtueMods = new VirtueCreationAdditionalParameters(template.getName());
		virtueMods.setSubnetId(subnetId);
		virtueMods.setSecurityGroupId(virtueSecurityGroupId);
		Collection<VirtualMachine> vms = awsVmManager.provisionVirtualMachineTemplates(user, windowsVmts, windowsFuture,
				virtueMods);
		vi.setVms(vms);

		// if (!linuxVmts.isEmpty()) {

		CompletableFuture<Collection<VirtualMachine>> linuxFuture = new CompletableFuture<Collection<VirtualMachine>>();
		CompletableFuture<VirtualMachine> xenFuture = new CompletableFuture<VirtualMachine>();
		// actually provisions xen host and then xen guests.
		xenHostManager.provisionXenHost(vi, linuxVmts, xenFuture, linuxFuture, virtueMods);
		// }
		windowsFuture.thenCombine(xenFuture, (Collection<VirtualMachine> winVms, VirtualMachine xen) -> {
			// When xen (really NFS) and all windows VM's are up
			// Add them to the windows startup services service

			// windowsNfsMountingService.addVirtueToQueue(vi);
			for (VirtualMachine windows : winVms) {
				windowsNfsMountingService.addWindowsStartupServices(xen, windows);
			}
			return winVms;
		}).thenAccept((Collection<VirtualMachine> finishedWindowsBoxes) -> {
			// Once startup services are done on windows machines, set VM state to running
			// and notify (notify saves to DB typically)
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
}
