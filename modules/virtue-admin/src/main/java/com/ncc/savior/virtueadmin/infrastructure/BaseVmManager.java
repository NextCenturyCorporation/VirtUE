package com.ncc.savior.virtueadmin.infrastructure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import com.ncc.savior.virtueadmin.model.VirtualMachine;

/**
 * 
 * Abstract class that handles the management of {@link IStateUpdateListener}s.
 * 
 *
 */
public abstract class BaseVmManager implements IVmManager {

	private Set<IUpdateListener<VirtualMachine>> vmUpdateListeners;

	protected BaseVmManager() {
		this.vmUpdateListeners = new LinkedHashSet<IUpdateListener<VirtualMachine>>();
	}

	@Override
	public void addVmUpdateListener(IUpdateListener<VirtualMachine> listener) {
		vmUpdateListeners.add(listener);
	}

	@Override
	public void removeVmUpdateListener(IUpdateListener<VirtualMachine> listener) {
		vmUpdateListeners.remove(listener);
	}

	protected void notifyOnUpdateVm(VirtualMachine vm) {
		ArrayList<VirtualMachine> vms = new ArrayList<VirtualMachine>(1);
		vms.add(vm);
		for (IUpdateListener<VirtualMachine> listener : vmUpdateListeners) {
			listener.updateElements(vms);
		}
	}

	protected void notifyOnUpdateVms(Collection<VirtualMachine> vms) {
		for (IUpdateListener<VirtualMachine> listener : vmUpdateListeners) {
			listener.updateElements(vms);
		}
	}
}
