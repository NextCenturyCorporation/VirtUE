package com.ncc.savior.virtueadmin.infrastructure;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import com.ncc.savior.virtueadmin.model.VmState;

import persistance.JpaVirtualMachine;

/**
 * 
 * Abstract class that handles the management of {@link IStateUpdateListener}s.
 * 
 *
 */
public abstract class BaseVmManager implements IVmManager {

	private Set<IVmUpdateListener> vmUpdateListeners;

	protected BaseVmManager() {
		this.vmUpdateListeners = new LinkedHashSet<IVmUpdateListener>();
	}

	@Override
	public void addVmUpdateListener(IVmUpdateListener listener) {
		vmUpdateListeners.add(listener);
	}

	@Override
	public void removeVmUpdateListener(IVmUpdateListener listener) {
		vmUpdateListeners.remove(listener);
	}

	protected void notifyOnUpdateVmState(String vmId, VmState state) {
		for (IVmUpdateListener listener : vmUpdateListeners) {
			listener.updateVmState(vmId, state);
		}
	}

	protected void notifyOnUpdateVms(Collection<JpaVirtualMachine> vms) {
		for (IVmUpdateListener listener : vmUpdateListeners) {
			listener.updateVms(vms);
		}
	}
}
