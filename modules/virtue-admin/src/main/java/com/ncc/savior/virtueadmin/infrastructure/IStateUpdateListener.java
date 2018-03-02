package com.ncc.savior.virtueadmin.infrastructure;

import com.ncc.savior.virtueadmin.model.AbstractVirtualMachine;
import com.ncc.savior.virtueadmin.model.VmState;

/**
 * Listener class to handle when a {@link AbstractVirtualMachine}'s {@link VmState} has
 * been updated.
 * 
 *
 */
public interface IStateUpdateListener {
	/**
	 * Called when a {@link AbstractVirtualMachine}'s {@link VmState} has been updated.
	 * 
	 * @param vmId
	 * @param state
	 */
	void updateVmState(String vmId, VmState state);
}
