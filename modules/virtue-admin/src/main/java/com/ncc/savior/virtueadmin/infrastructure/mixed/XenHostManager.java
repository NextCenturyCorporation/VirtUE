package com.ncc.savior.virtueadmin.infrastructure.mixed;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.ec2.model.InstanceType;
import com.ncc.savior.virtueadmin.data.IActiveVirtueDao;
import com.ncc.savior.virtueadmin.infrastructure.IKeyManager;
import com.ncc.savior.virtueadmin.infrastructure.IUpdateListener;
import com.ncc.savior.virtueadmin.infrastructure.aws.AwsEc2Wrapper;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.OS;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VirtualMachineTemplate;
import com.ncc.savior.virtueadmin.model.VirtueInstance;
import com.ncc.savior.virtueadmin.model.VmState;
import com.ncc.savior.virtueadmin.util.JavaUtil;

public class XenHostManager {
	private static final Logger logger = LoggerFactory.getLogger(XenAwsMixCloudManager.class);
	private VirtualMachineTemplate xenVmTemplate;
	private IUpdateListener<VirtualMachine> notifier;
	private AwsEc2Wrapper ec2Wrapper;
	private Collection<String> securityGroups;
	private String xenKeyName;
	private InstanceType xenInstanceType;
	private XenHostVmUpdater updater;
	protected IActiveVirtueDao vmDao;
	private String serverUser;

	public XenHostManager(IKeyManager keyManager, AwsEc2Wrapper ec2Wrapper, IActiveVirtueDao xenVmDao,
			IUpdateListener<VirtualMachine> actualVmNotifier, Collection<String> securityGroups, String xenKeyName,
			InstanceType xenInstanceType) {
		this.notifier = actualVmNotifier;
		this.ec2Wrapper = ec2Wrapper;
		this.securityGroups = securityGroups;
		this.xenKeyName = xenKeyName;
		this.xenInstanceType = xenInstanceType;
		this.vmDao = xenVmDao;
		this.serverUser = System.getProperty("user.name");
		IUpdateListener<VirtualMachine> xenListener = new IUpdateListener<VirtualMachine>() {
			@Override
			public void updateElements(Collection<VirtualMachine> elements) {
				xenVmDao.updateVms(elements);
			}
		};
		this.updater = new XenHostVmUpdater(ec2Wrapper.getEc2(), xenListener, keyManager);
		String templatePath = "ami-e156839c";
		String xenLoginUser = "ec2-user";
		this.xenVmTemplate = new VirtualMachineTemplate(UUID.randomUUID().toString(), "XenTemplate", OS.LINUX,
				templatePath, new ArrayList<ApplicationDefinition>(), xenLoginUser, false, new Date(0), "system");
	}

	public XenHostManager(IKeyManager keyManager, AwsEc2Wrapper ec2Wrapper, IActiveVirtueDao xenVmDao,
			IUpdateListener<VirtualMachine> notifier, String securityGroupsCommaSeparated, String xenKeyName,
			String xenInstanceType) {
		this(keyManager, ec2Wrapper, xenVmDao, notifier, splitOnComma(securityGroupsCommaSeparated), xenKeyName,
				InstanceType.fromValue(xenInstanceType));
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

	public void provisionXenHost(VirtueInstance virtue, Collection<VirtualMachineTemplate> linuxVmts) {
		VirtualMachine xenVm = ec2Wrapper.provisionVm(xenVmTemplate,
				"Xen-" + serverUser + "-" + virtue.getUsername() + "-", securityGroups,
				xenKeyName, xenInstanceType);
		xenVm.setId(virtue.getId());
		ArrayList<VirtualMachine> xenVms = new ArrayList<VirtualMachine>();
		xenVms.add(xenVm);
		vmDao.updateVms(xenVms);
		updater.addVmToProvisionPipeline(xenVms);
		final String id = virtue.getId();
		for (VirtualMachineTemplate vmt : linuxVmts) {
			VirtualMachine vm = new VirtualMachine(UUID.randomUUID().toString(), "",
					new ArrayList<ApplicationDefinition>(), VmState.CREATING, vmt.getOs(), "", "", 22, "", "", "", "");
			virtue.getVms().add(vm);
		}
		Runnable r = new Runnable() {

			@Override
			public void run() {
				Optional<VirtualMachine> vmo = vmDao.getXenVm(id);
				while (!vmo.isPresent() || !VmState.RUNNING.equals(vmo.get().getState())) {
					JavaUtil.sleepAndLogInterruption(2000);
					vmo = vmDao.getXenVm(id);
				}
				// TODO Create vms from templates but use the VM instances already stored in the
				// virtue.

				// TODO make sure VM status is updated via notifier

				// TEST CODE TO BE REMOVED ONCE XEN VM's ARE CREATED AND UPDATED
				for (VirtualMachine vm : virtue.getVms()) {
					vm.setState(VmState.RUNNING);
				}
				notifier.updateElements(virtue.getVms());
				// END TEST CODE

				logger.info("Create vms here " + linuxVmts);
			}
		};
		Thread t = new Thread(r, "XenProvisioner-" + id);
		t.start();
	}

	public void deleteVirtue(String id, Collection<VirtualMachine> linuxVms) {
		// TODO get XenVmManager for id
		// TODO tell XenManager to delete its vms
		// TODO schedule once Vm's are deleted, XenManager will delete itself.
		Optional<VirtualMachine> vm = vmDao.getXenVm(id);
		if (vm.isPresent()) {
			ArrayList<VirtualMachine> vms = new ArrayList<VirtualMachine>();
			vms.add(vm.get());
			ec2Wrapper.deleteVirtualMachines(vms);
		}

	}

	public void startVirtue(VirtueInstance virtueInstance, Collection<VirtualMachine> linuxVms) {
		// TODO Auto-generated method stub

	}

	public void stopVirtue(VirtueInstance virtueInstance, Collection<VirtualMachine> linuxVms) {
		// TODO Auto-generated method stub

	}

	protected void setServerUser(String serverUser) {
		if (serverUser != null && !serverUser.trim().equals("")) {
			this.serverUser = serverUser;
		}
	}

}
