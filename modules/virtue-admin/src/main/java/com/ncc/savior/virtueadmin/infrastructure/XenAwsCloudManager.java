package com.ncc.savior.virtueadmin.infrastructure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.ec2.AmazonEC2;
import com.ncc.savior.virtueadmin.infrastructure.aws.AsyncAwsEc2VmManager;
import com.ncc.savior.virtueadmin.infrastructure.aws.AwsVmUpdater;
import com.ncc.savior.virtueadmin.infrastructure.aws.VirtueAwsEc2Provider;
import com.ncc.savior.virtueadmin.infrastructure.pipelining.AwsNetworkingUpdateComponent;
import com.ncc.savior.virtueadmin.infrastructure.pipelining.AwsRenamingComponent;
import com.ncc.savior.virtueadmin.infrastructure.pipelining.TestReachabilityComponent;
import com.ncc.savior.virtueadmin.infrastructure.pipelining.UpdatePipeline;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.OS;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VirtualMachineTemplate;
import com.ncc.savior.virtueadmin.model.VirtueInstance;
import com.ncc.savior.virtueadmin.model.VirtueTemplate;
import com.ncc.savior.virtueadmin.model.VirtueUser;
import com.ncc.savior.virtueadmin.model.VmState;

public class XenAwsCloudManager implements ICloudManager {
	private static Logger logger = LoggerFactory.getLogger(XenAwsCloudManager.class);

	private String xenAmi;
	private AsyncAwsEc2VmManager ec2Manager;
	private VirtualMachineTemplate xenVirtualMachineTemplate;
	private UpdatePipeline<VirtualMachine> provisionPipeline;
	private AsyncAwsEc2VmManager xenGenerator;
	private ScheduledExecutorService executor;
	private Map<String, XenGenerationTask> tasks;

	private VirtueAwsEc2Provider ec2Provider;

	public XenAwsCloudManager(AsyncAwsEc2VmManager ec2Manager, IUpdateListener<VirtualMachine> updateListener,
			String xenAmi, AmazonEC2 ec2, IKeyManager keyManager, String region, String awsProfile) {
		this.ec2Manager = ec2Manager;
		this.xenAmi = xenAmi;
		this.tasks = new HashMap<String, XenGenerationTask>();
		this.executor = Executors.newScheduledThreadPool(3, new ThreadFactory() {

			private int i = 1;

			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r, "XenUpdater-" + i);
				i++;
				return t;
			}
		});
		this.provisionPipeline = new UpdatePipeline<VirtualMachine>(updateListener, "Xen-provision");
		provisionPipeline.addPipelineComponent(new AwsRenamingComponent(executor, ec2));
		provisionPipeline.addPipelineComponent(new AwsNetworkingUpdateComponent(executor, ec2));
		provisionPipeline.addPipelineComponent(new TestReachabilityComponent(executor, keyManager, true));
		String xenLoginUser = "admin";
		this.xenVirtualMachineTemplate = new VirtualMachineTemplate(UUID.randomUUID().toString(), "Xen-Test", OS.LINUX,
				this.xenAmi, new ArrayList<ApplicationDefinition>(), xenLoginUser, true, new Date(0), "system");

		IUpdateListener<VirtualMachine> xenVmHostNotifier = new IUpdateListener<VirtualMachine>() {

			@Override
			public void updateElements(Collection<VirtualMachine> elements) {
				for (VirtualMachine vm : elements) {
					if (VmState.RUNNING.equals(vm.getState())) {
						XenGenerationTask task = tasks.get(vm.getId());
						if (task == null) {
							logger.error("Error: Unable to find task for newly running Xen instance");
						}
						// TODO create xen vm manager
						// TODO provision xen
					}
				}

			}
		};
		IVmUpdater updater = new AwsVmUpdater(ec2, xenVmHostNotifier, keyManager, false);
		xenGenerator = new AsyncAwsEc2VmManager(updater, keyManager, ec2Provider);
	}

	@Override
	public void deleteVirtue(VirtueInstance virtueInstance) {
		// TODO Auto-generated method stub

	}

	@Override
	public VirtueInstance createVirtue(VirtueUser user, VirtueTemplate template) throws Exception {
		Collection<VirtualMachineTemplate> vmts = template.getVmTemplates();
		Collection<VirtualMachineTemplate> windowsVmts = new ArrayList<VirtualMachineTemplate>(vmts.size());
		Collection<VirtualMachineTemplate> linuxVmts = new ArrayList<VirtualMachineTemplate>(vmts.size());
		Collection<VirtualMachine> vms = ec2Manager.provisionVirtualMachineTemplates(user, windowsVmts);

		VirtueInstance vi = new VirtueInstance(template, user.getUsername(), vms);

		provisionXenVms(linuxVmts, vi, user);
		return vi;
	}

	private void provisionXenVms(Collection<VirtualMachineTemplate> linuxVmts, VirtueInstance vi, VirtueUser user) {
		VirtualMachine xenVm = xenGenerator.provisionVirtualMachineTemplate(user, xenVirtualMachineTemplate);
		XenGenerationTask task = new XenGenerationTask(xenVm.getId(), linuxVmts, vi);
		tasks.put(xenVm.getId(), task);
	}

	@Override
	public VirtueInstance startVirtue(VirtueInstance virtueInstance) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public VirtueInstance stopVirtue(VirtueInstance virtueInstance) {
		// TODO Auto-generated method stub
		return null;
	}

	private static class XenGenerationTask {

		private VirtueInstance virtue;
		private Collection<VirtualMachineTemplate> vmtsToBeCreated;
		private String xenVmId;

		public XenGenerationTask(String id, Collection<VirtualMachineTemplate> linuxVmts, VirtueInstance vi) {
			this.xenVmId = id;
			this.vmtsToBeCreated = linuxVmts;
			this.virtue = vi;
		}
	}
}
