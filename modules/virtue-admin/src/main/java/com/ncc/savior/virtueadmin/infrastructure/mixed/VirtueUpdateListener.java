package com.ncc.savior.virtueadmin.infrastructure.mixed;

import java.util.Collection;

import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VirtueInstance;

public interface VirtueUpdateListener {
	public void updateVirtue(VirtueInstance vi);

	public void updateVirtualMachines(Collection<VirtualMachine> vms);
}