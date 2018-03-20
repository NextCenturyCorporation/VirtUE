package com.ncc.savior.virtueadmin.infrastructure.pipelining;

import java.util.Collection;
import java.util.concurrent.ScheduledExecutorService;

import com.ncc.savior.virtueadmin.model.VirtualMachine;

public class NetworkingClearingComponent extends BaseGroupedVmPipelineComponent {

	public NetworkingClearingComponent(ScheduledExecutorService executor) {
		super(executor, true, 100, 500);
	}

	@Override
	protected void onExecute(Collection<VirtualMachine> vms) {
		if (!vms.isEmpty()) {
			for (VirtualMachine vm : vms) {
				vm.setHostname(null);
				vm.setIpAddress(null);
			}
			doOnSuccess(vms);
		}
	}
}
