package com.ncc.savior.virtueadmin.infrastructure.statemachine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

import org.springframework.statemachine.StateMachine;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceType;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VirtualMachineTemplate;
import com.ncc.savior.virtueadmin.model.VirtueUser;
import com.ncc.savior.virtueadmin.model.VmState;

public class StateMachineManager {

	private static final int SSH_PORT = 0;
	private TreeMap<String, StateMachine> stateMachines;
	private ScheduledExecutorService executor;
	private InstanceType instanceType;
	private String serverKeyName;
	private Collection<String> defaultSecurityGroups;
	private AmazonEC2 ec2;

	public StateMachineManager() {
		this.stateMachines = new TreeMap<String, StateMachine>();
		this.executor = Executors.newScheduledThreadPool(5, new ThreadFactory() {
			private int i;

			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r, "StateMachineManager-" + i);
				i++;
				return t;
			}
		});
	}

	public void add(StateMachine<ProvisionStates, StateEvents> stateMachine) {
		// Map<Object, Object> vars = stateMachine.getExtendedState().getVariables();
		// vars.put(StateMachineCloudManager.KEY_STATE_MACHINE_MANAGER, this);
		// ScheduledFuture<?> future = executor.schedule(getProvisionXen(), 0,
		// TimeUnit.SECONDS);
		// vars.put(StateMachineCloudManager.KEY_FUTURE, future);

	}

	public Runnable getProvisionXen() {
		return new Runnable() {

			@Override
			public void run() {
				// provisionVm(user, vmt, namePrefix);
				//
				// stateMachine.sendEvent(ProvisionEvents.PROVISION_XEN);
			}
		};

	}

	private VirtualMachine provisionVm(VirtueUser user, VirtualMachineTemplate vmt, String namePrefix) {
		RunInstancesRequest runInstancesRequest = new RunInstancesRequest();

		String templatePath = vmt.getTemplatePath();
		runInstancesRequest = runInstancesRequest.withImageId(templatePath).withInstanceType(instanceType)
				.withMinCount(1).withMaxCount(1).withKeyName(serverKeyName).withSecurityGroups(defaultSecurityGroups);
		RunInstancesResult result = ec2.runInstances(runInstancesRequest);

		List<Instance> instances = result.getReservation().getInstances();
		if (instances.size() != 1) {
			throw new RuntimeException("Created more than 1 instance when only 1 was expected!");
		}
		Instance instance = instances.get(0);

		String name = namePrefix + instance.getInstanceId();
		String loginUsername = vmt.getLoginUser();
		String privateKeyName = serverKeyName;
		VirtualMachine vm = new VirtualMachine(UUID.randomUUID().toString(), name,
				new ArrayList<ApplicationDefinition>(vmt.getApplications()), VmState.CREATING, vmt.getOs(),
				instance.getInstanceId(), instance.getPublicDnsName(), SSH_PORT, loginUsername, null, privateKeyName,
				instance.getPublicIpAddress());
		return vm;
	}

}
