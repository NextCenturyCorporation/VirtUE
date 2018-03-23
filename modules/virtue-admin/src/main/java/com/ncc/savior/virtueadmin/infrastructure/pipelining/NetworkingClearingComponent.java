package com.ncc.savior.virtueadmin.infrastructure.pipelining;

import java.util.Collection;
import java.util.concurrent.ScheduledExecutorService;

import com.ncc.savior.virtueadmin.model.VirtualMachine;

public class NetworkingClearingComponent extends BaseGroupedVmPipelineComponent<VirtualMachine> {

	public NetworkingClearingComponent(ScheduledExecutorService executor) {
		super(executor, true, 100, 500);
	}

	@Override
	protected void onExecute(Collection<PipelineWrapper<VirtualMachine>> wrappers) {
		if (!wrappers.isEmpty()) {
			for (PipelineWrapper<VirtualMachine> wrapper : wrappers) {
				VirtualMachine vm = wrapper.get();
				vm.setHostname(null);
				vm.setIpAddress(null);
			}
			doOnSuccess(wrappers);
		}
	}

}
