package com.ncc.savior.virtueadmin.infrastructure.pipelining;

import java.util.ArrayList;
import java.util.concurrent.ScheduledExecutorService;

import com.ncc.savior.virtueadmin.model.persistance.JpaVirtualMachine;

public class NetworkingClearingComponent extends BaseGroupedVmPipelineComponent {

	public NetworkingClearingComponent(ScheduledExecutorService executor) {
		super(executor, true, 100, 500);
	}

	@Override
	protected void onExecute(ArrayList<JpaVirtualMachine> vms) {
		if (!vms.isEmpty()) {
			for (JpaVirtualMachine vm : vms) {
				vm.setHostname(null);
				vm.setIpAddress(null);
			}
			doOnSuccess(vms);
		}
	}
}
