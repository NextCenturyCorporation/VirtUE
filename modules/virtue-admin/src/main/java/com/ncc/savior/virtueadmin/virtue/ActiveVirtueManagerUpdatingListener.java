package com.ncc.savior.virtueadmin.virtue;

import java.util.Collection;

import com.ncc.savior.virtueadmin.infrastructure.IVmUpdateListener;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VmState;

public class ActiveVirtueManagerUpdatingListener implements IVmUpdateListener {
	private IActiveVirtueManager virtueManager;

	public ActiveVirtueManagerUpdatingListener(IActiveVirtueManager virtueManager) {
		this.virtueManager = virtueManager;
	}

	@Override
	public void updateVmState(String vmId, VmState state) {
		virtueManager.updateVmState(vmId, state);
	}

	@Override
	public void updateVm(Collection<VirtualMachine> vms) {
		virtueManager.updateVms(vms);
	}

}
