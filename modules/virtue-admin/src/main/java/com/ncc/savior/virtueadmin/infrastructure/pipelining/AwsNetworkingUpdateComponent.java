package com.ncc.savior.virtueadmin.infrastructure.pipelining;

import java.util.ArrayList;
import java.util.concurrent.ScheduledExecutorService;

import com.amazonaws.services.ec2.AmazonEC2;
import com.ncc.savior.virtueadmin.infrastructure.aws.AwsUtil;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VmState;
import com.ncc.savior.virtueadmin.util.JavaUtil;

/**
 * Component of an {@link IUpdatePipeline} that will retrieve the networking (IP
 * address and hostname) from AWS and add it to the {@link VirtualMachine}
 * object.
 */
public class AwsNetworkingUpdateComponent extends BaseGroupedVmPipelineComponent {

	private AmazonEC2 ec2;

	public AwsNetworkingUpdateComponent(ScheduledExecutorService executor, AmazonEC2 ec2) {
		super(executor, false, 1000, 5000);
		this.ec2 = ec2;
	}

	@Override
	protected void onExecute(ArrayList<VirtualMachine> vms) {
		ArrayList<VirtualMachine> updated = new ArrayList<VirtualMachine>();
		AwsUtil.updateNetworking(ec2, vms);
		for (VirtualMachine vm : vms) {
			if (JavaUtil.isNotEmpty(vm.getHostname())) {
				vm.setState(VmState.LAUNCHING);
				updated.add(vm);
			}
		}
		if (!updated.isEmpty()) {
			doOnSuccess(vms);
		}
	}
}
