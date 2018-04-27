package com.ncc.savior.virtueadmin.infrastructure.pipelining;

import java.util.Collection;
import java.util.concurrent.ScheduledExecutorService;

import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VmState;

/**
 * Automatically sets the state of the vms and returns success.
 *
 */
public class SetStatusComponent extends BaseGroupedVmPipelineComponent<VirtualMachine> {

	private VmState state;

	public SetStatusComponent(ScheduledExecutorService executor, VmState state) {
		super(executor, true, 100, 500);
		this.state = state;
	}

	@Override
	protected void onExecute(Collection<PipelineWrapper<VirtualMachine>> wrappers) {
		if (!wrappers.isEmpty()) {
			for (PipelineWrapper<VirtualMachine> wrapper : wrappers) {
				wrapper.get().setState(state);
			}
			doOnSuccess(wrappers);
		}
	}
}
