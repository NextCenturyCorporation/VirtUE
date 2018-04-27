package com.ncc.savior.virtueadmin.infrastructure.pipelining;

import java.util.Collection;
import java.util.concurrent.ScheduledExecutorService;

import com.ncc.savior.virtueadmin.model.VirtualMachine;

/**
 * Clears the networking information (hostnames, ip address) for a vm. This is
 * useful when the VM is shut down and the networking information could change
 * later.
 * 
 *
 */
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
				vm.setInternalIpAddress(null);
				vm.setInternalHostname(null);
			}
			doOnSuccess(wrappers);
		}
	}

}
