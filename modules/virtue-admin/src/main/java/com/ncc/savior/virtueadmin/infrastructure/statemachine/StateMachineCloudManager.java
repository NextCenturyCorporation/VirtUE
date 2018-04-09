package com.ncc.savior.virtueadmin.infrastructure.statemachine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;

import com.ncc.savior.virtueadmin.util.JavaUtil;

public class StateMachineCloudManager {// implements ICloudManager, StateMachineKeys {
	private static final Logger logger = LoggerFactory.getLogger(StateMachineCloudManager.class);

	@Autowired
	private StateMachineFactory<ProvisionStates, StateEvents> provisionerFactory;
	// private StateMachineManager stateMachineManager;
	// private AwsEc2VmManager awsVmManager;

	public StateMachineCloudManager() {

	}

	public void init() {
		for (int i = 0; i < 10; i++) {
			StateMachine<ProvisionStates, StateEvents> s = provisionerFactory.getStateMachine("" + i);
			s.start();
			s.sendEvent(StateEvents.SUCCESS);
		}
		JavaUtil.sleepAndLogInterruption(200000);
	}

	// @Override
	// public void deleteVirtue(VirtueInstance virtueInstance) {
	// // TODO Auto-generated method stub
	//
	// }
	//
	// @Override
	// public VirtueInstance createVirtue(VirtueUser user, VirtueTemplate template)
	// throws Exception {
	// Collection<VirtualMachineTemplate> linuxVmts = new
	// ArrayList<VirtualMachineTemplate>();
	// Collection<VirtualMachineTemplate> windowsVmts = new
	// ArrayList<VirtualMachineTemplate>();
	// Collection<VirtualMachineTemplate> vmts = template.getVmTemplates();
	// for (VirtualMachineTemplate vmt : vmts) {
	// if (OS.LINUX.equals(vmt.getOs())) {
	// linuxVmts.add(vmt);
	// } else if (OS.WINDOWS.equals(vmt.getOs())) {
	// windowsVmts.add(vmt);
	// }
	// }
	//
	// // TODO remove this comment
	// // Collection<VirtualMachine> vms =
	// // awsVmManager.provisionVirtualMachineTemplates(user, windowsVmts);
	// Collection<VirtualMachine> vms = new ArrayList<VirtualMachine>();
	// VirtueInstance vi = new VirtueInstance(template, user.getUsername(), vms);
	//
	// String id = vi.getId();
	// StateMachine<ProvisionStates, StateEvents> stateMachine =
	// provisionerFactory.getStateMachine(id);
	// ExtendedState es = stateMachine.getExtendedState();
	// es.getVariables().put(KEY_VIRTUE_ID, id);
	// es.getVariables().put(KEY_XEN_VMTS, linuxVmts);
	// es.getVariables().put(KEY_USER, user);
	// stateMachine.start();
	// stateMachineManager.add(stateMachine);
	//
	// return vi;
	// }
	//
	// @Override
	// public VirtueInstance startVirtue(VirtueInstance virtueInstance) {
	// // TODO Auto-generated method stub
	// return null;
	// }
	//
	// @Override
	// public VirtueInstance stopVirtue(VirtueInstance virtueInstance) {
	// // TODO Auto-generated method stub
	// return null;
	// }

}