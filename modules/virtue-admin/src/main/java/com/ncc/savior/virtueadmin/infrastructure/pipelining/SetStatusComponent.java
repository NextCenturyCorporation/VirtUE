package com.ncc.savior.virtueadmin.infrastructure.pipelining;

import java.util.Collection;
import java.util.concurrent.ScheduledExecutorService;

import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VmState;

/**
 * Automatically sets the state of the vms and returns success.
 *
 */
public class SetStatusComponent extends BaseGroupedVmPipelineComponent {

	private VmState state;

	public SetStatusComponent(ScheduledExecutorService executor, VmState state) {
		super(executor, true, 100, 500);
		this.state = state;
	}

	@Override
	protected void onExecute(Collection<VirtualMachine> vms) {
		if (!vms.isEmpty()) {
			for (VirtualMachine vm : vms) {
				vm.setState(state);
			}
			doOnSuccess(vms);
		}
	}
}
