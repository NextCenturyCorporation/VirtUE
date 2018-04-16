package com.ncc.savior.virtueadmin.infrastructure.pipelining;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ScheduledExecutorService;

import com.amazonaws.services.ec2.AmazonEC2;
import com.ncc.savior.virtueadmin.infrastructure.aws.AwsUtil;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VmState;

public class AwsGetStatusComponent extends BaseGroupedVmPipelineComponent<VirtualMachine> {

	private AmazonEC2 ec2;
	private Collection<VmState> exitStates;

	public AwsGetStatusComponent(ScheduledExecutorService executor, AmazonEC2 ec2, VmState exitState) {
		super(executor, true, 200, 1000);
		this.ec2 = ec2;
		this.exitStates = new ArrayList<VmState>(1);
		this.exitStates.add(exitState);
	}

	public AwsGetStatusComponent(ScheduledExecutorService executor, AmazonEC2 ec2, Collection<VmState> exitStates) {
		super(executor, true, 200, 1000);
		this.ec2 = ec2;
		this.exitStates = exitStates;
	}

	@Override
	protected void onExecute(Collection<PipelineWrapper<VirtualMachine>> wrappers) {
		Collection<PipelineWrapper<VirtualMachine>> successfulVms = new ArrayList<PipelineWrapper<VirtualMachine>>();
		Collection<PipelineWrapper<VirtualMachine>> errorVms = new ArrayList<PipelineWrapper<VirtualMachine>>();
		Collection<VirtualMachine> vms = unwrap(wrappers);
		vms = AwsUtil.updateStatusOnVms(ec2, vms);
		for (PipelineWrapper<VirtualMachine> wrapper : wrappers) {
			VirtualMachine vm = wrapper.get();
			if (exitStates.contains(vm.getState())) {
				successfulVms.add(wrapper);
			} else if (VmState.ERROR.equals(vm.getState())) {
				errorVms.add(wrapper);
			}
		}
		if (!successfulVms.isEmpty()) {
			doOnSuccess(successfulVms);
		}
		if (!errorVms.isEmpty()) {
			doOnFailure(errorVms);
		}
	}
}
