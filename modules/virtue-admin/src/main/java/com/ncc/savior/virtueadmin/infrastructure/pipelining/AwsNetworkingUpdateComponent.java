package com.ncc.savior.virtueadmin.infrastructure.pipelining;

import java.util.ArrayList;
import java.util.Collection;
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
public class AwsNetworkingUpdateComponent extends BaseGroupedVmPipelineComponent<VirtualMachine> {

	private AmazonEC2 ec2;
	private boolean usePublicDns;

	public AwsNetworkingUpdateComponent(ScheduledExecutorService executor, AmazonEC2 ec2, boolean usePublicDns) {
		super(executor, false, 1000, 5000);
		this.ec2 = ec2;
		this.usePublicDns=usePublicDns;
	}

	@Override
	protected void onExecute(Collection<PipelineWrapper<VirtualMachine>> wrappers) {
		ArrayList<PipelineWrapper<VirtualMachine>> updated = new ArrayList<PipelineWrapper<VirtualMachine>>();
		AwsUtil.updateNetworking(ec2, unwrap(wrappers), usePublicDns);
		for (PipelineWrapper<VirtualMachine> wrapper : wrappers) {
			VirtualMachine vm = wrapper.get();
			if (JavaUtil.isNotEmpty(vm.getHostname())) {
				vm.setState(VmState.LAUNCHING);
				updated.add(wrapper);
			}
		}
		if (!updated.isEmpty()) {
			doOnSuccess(updated);
		}
	}

}
