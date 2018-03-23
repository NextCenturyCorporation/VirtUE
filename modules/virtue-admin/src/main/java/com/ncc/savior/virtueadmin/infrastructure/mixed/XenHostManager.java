package com.ncc.savior.virtueadmin.infrastructure.mixed;

import java.util.ArrayList;
import java.util.Collection;

import com.ncc.savior.virtueadmin.data.IActiveVirtueDao;
import com.ncc.savior.virtueadmin.infrastructure.IKeyManager;
import com.ncc.savior.virtueadmin.infrastructure.IUpdateListener;
import com.ncc.savior.virtueadmin.infrastructure.IVmUpdater;
import com.ncc.savior.virtueadmin.infrastructure.aws.AsyncAwsEc2VmManager;
import com.ncc.savior.virtueadmin.infrastructure.aws.VirtueAwsEc2Provider;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VirtualMachineTemplate;
import com.ncc.savior.virtueadmin.model.VirtueInstance;
import com.ncc.savior.virtueadmin.model.VirtueUser;
import com.ncc.savior.virtueadmin.model.VmState;

public class XenHostManager {
	private IXenVmCreationScheduleManager vmCreationScheduleManager;
	private AsyncAwsEc2VmManager xenVmManager;
	private VirtueUser XenManagementUser;
	private VirtualMachineTemplate xenVmTemplate;
	private IUpdateListener<VirtualMachine> notifier;
	private IActiveVirtueDao activeVirtueDao;

	public XenHostManager(IKeyManager keyManager, VirtueAwsEc2Provider ec2Provider, IActiveVirtueDao activeVirtueDao) {
		this.activeVirtueDao = activeVirtueDao;
		this.notifier = new IUpdateListener<VirtualMachine>() {

			@Override
			public void updateElements(Collection<VirtualMachine> vms) {
				for (VirtualMachine vm : vms) {
					if (VmState.RUNNING.equals(vm.getState())) {
						initiateVmCreationOnXen(vm);
					}
				}
			}
		};
		IVmUpdater updater = new XenHostCreationVmUpdater(ec2Provider.getEc2(), notifier, keyManager);
		this.xenVmManager = new AsyncAwsEc2VmManager(updater, keyManager, ec2Provider);
		XenManagementUser = new VirtueUser("XenManager", new ArrayList<String>());
	}

	protected void initiateVmCreationOnXen(VirtualMachine vm) {
		Collection<VirtualMachineTemplate> linuxVmts = vmCreationScheduleManager.getTemplates(vm.getName());
		// TODO create XenVmManager
		// TOOD provision XenVms
		// TODO save XenVmManager
		Collection<VirtualMachine> vms = new ArrayList<VirtualMachine>();
		VirtueInstance virtue = activeVirtueDao.getVirtueInstance(vm.getName()).get();
		virtue.getVms().addAll(vms);
		activeVirtueDao.updateVirtue(virtue);
	}

	public VirtualMachine provisionXenHost(String xenHostName, Collection<VirtualMachineTemplate> linuxVmts,
			VirtueUser user) {
		VirtualMachine vm = xenVmManager.provisionVirtualMachineTemplate(XenManagementUser, xenVmTemplate);
		vm.setName(xenHostName);
		Collection<VirtualMachine> vms = new ArrayList<VirtualMachine>(1);
		notifier.updateElements(vms);

		vmCreationScheduleManager.addTemplates(xenHostName, linuxVmts);
		return vm;
	}

	public void deleteVirtue(String id, Collection<VirtualMachine> linuxVms) {
		// TODO get XenVmManager for id
		// TODO tell XenManager to delete itself
		// TODO schedule once Vm's are deleted, XenManager will delete itself.

	}

}
