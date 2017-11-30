package com.ncc.savior.virtueadmin.infrastructure;

import java.util.LinkedHashSet;
import java.util.Set;

import com.ncc.savior.virtueadmin.model.VmState;

public abstract class BaseVmManager implements IVmManager {

	private Set<IStateUpdateListener> stateUpdateListeners;

	protected BaseVmManager() {
		this.stateUpdateListeners = new LinkedHashSet<IStateUpdateListener>();
	}

	@Override
	public void addStateUpdateListener(IStateUpdateListener listener) {
		stateUpdateListeners.add(listener);
	}

	@Override
	public void removeStateUpdateListener(IStateUpdateListener listener) {
		stateUpdateListeners.remove(listener);
	}

	protected void updateVmState(String vmId, VmState state) {
		for (IStateUpdateListener listener : stateUpdateListeners) {
			listener.updateVmState(vmId, state);
		}
	}
}
