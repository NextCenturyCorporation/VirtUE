package com.ncc.savior.virtueadmin.infrastructure;

import java.util.Collection;

import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VmState;

/**
 * Listener class to handle when a {@link VirtualMachine}'s {@link VmState} has
 * been updated.
 * 
 *
 */
public interface IVmUpdateListener {
	/**
	 * Called when a {@link VirtualMachine}'s {@link VmState} has been updated.
	 * 
	 * @param vmId
	 * @param state
	 */
	void updateVmState(String vmId, VmState state);

	void updateVms(Collection<VirtualMachine> vms);
}
