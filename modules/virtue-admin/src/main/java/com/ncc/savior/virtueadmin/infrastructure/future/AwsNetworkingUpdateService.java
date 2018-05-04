package com.ncc.savior.virtueadmin.infrastructure.future;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ScheduledExecutorService;

import com.amazonaws.services.ec2.AmazonEC2;
import com.ncc.savior.virtueadmin.infrastructure.aws.AwsUtil;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.util.JavaUtil;

/**
 * Service which will try get the networking information for an AWS instance. If
 * usePublicDns is true, it will get the public DNS and IP, otherwise, it will
 * only get the internal hostname and IP. AWS internal DNS and IP are always
 * retrieved and stored in the {@link VirtualMachine}s internal DNS and IP
 * variables. This service will retry until successful and then complete the
 * future.
 * 
 *
 */
public class AwsNetworkingUpdateService
		extends BaseGroupedScheduledCompletableFutureService<VirtualMachine, VirtualMachine, Void> {

	private AmazonEC2 ec2;
	private boolean usePublicDns;

	public AwsNetworkingUpdateService(ScheduledExecutorService executor, AmazonEC2 ec2, boolean usePublicDns) {
		super(executor, false, 1000, 5000);
		this.ec2 = ec2;
		this.usePublicDns = usePublicDns;
	}

	@Override
	protected void onExecute(
			Collection<BaseGroupedScheduledCompletableFutureService<VirtualMachine, VirtualMachine, Void>.Wrapper> wrappers) {
		ArrayList<VirtualMachine> updated = new ArrayList<VirtualMachine>();
		Collection<VirtualMachine> vms = unwrapParameter(wrappers);
		AwsUtil.updateNetworking(ec2, vms, usePublicDns);
		for (VirtualMachine vm : vms) {
			if (JavaUtil.isNotEmpty(vm.getHostname())) {
				updated.add(vm);
			}
		}
		ArrayList<BaseCompletableFutureService<VirtualMachine, VirtualMachine, Void>.Wrapper> completedWrappers = new ArrayList<Wrapper>();
		if (!updated.isEmpty()) {
			for (VirtualMachine updatedVm : updated) {
				for (BaseCompletableFutureService<VirtualMachine, VirtualMachine, Void>.Wrapper wrapper : wrappers) {
					if (wrapper.param.getId().equals(updatedVm.getId())) {
						wrapper.result = updatedVm;
						completedWrappers.add(wrapper);
					}
				}
			}
		}
		onSuccess(completedWrappers);
	}

	@Override
	protected String getServiceName() {
		return "AwsNetworkingUpdaterService";
	}

}
