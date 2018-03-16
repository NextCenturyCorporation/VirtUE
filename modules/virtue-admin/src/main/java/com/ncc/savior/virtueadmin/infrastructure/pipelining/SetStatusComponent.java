package com.ncc.savior.virtueadmin.infrastructure.pipelining;

import java.util.ArrayList;
import java.util.concurrent.ScheduledExecutorService;

import com.ncc.savior.virtueadmin.model.VmState;
import com.ncc.savior.virtueadmin.model.persistance.JpaVirtualMachine;

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
	protected void onExecute(ArrayList<JpaVirtualMachine> vms) {
		if (!vms.isEmpty()) {
			for (JpaVirtualMachine vm : vms) {
				vm.setState(state);
			}
			doOnSuccess(vms);
		}
	}
}
