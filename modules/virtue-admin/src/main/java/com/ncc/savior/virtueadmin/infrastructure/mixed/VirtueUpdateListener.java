package com.ncc.savior.virtueadmin.infrastructure.mixed;

import java.util.Collection;

import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VirtueInstance;

/**
 * Listener class for when virtues or VM's have been updated and something needs
 * to be notified. Typically the notifications are used to pass the updated data
 * back to a DAO such that it can store it in whatever storage it has.
 * 
 *
 */
public interface VirtueUpdateListener {
	public void updateVirtue(VirtueInstance vi);

	public void updateVirtualMachines(Collection<VirtualMachine> vms);
}