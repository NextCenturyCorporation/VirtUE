package com.ncc.savior.virtueadmin.virtue;

import java.util.Collection;

import com.ncc.savior.virtueadmin.data.IActiveVirtueDao;
import com.ncc.savior.virtueadmin.infrastructure.IVmUpdateListener;
import com.ncc.savior.virtueadmin.model.VmState;
import com.ncc.savior.virtueadmin.model.persistance.JpaVirtualMachine;

public class ActiveVirtueManagerUpdatingListener implements IVmUpdateListener {
	private IActiveVirtueDao virtueManager;

	public ActiveVirtueManagerUpdatingListener(IActiveVirtueDao virtueManager) {
		this.virtueManager = virtueManager;
	}

	@Override
	public void updateVmState(String vmId, VmState state) {
		virtueManager.updateVmState(vmId, state);
	}

	@Override
	public void updateVms(Collection<JpaVirtualMachine> vms) {
		virtueManager.updateVms(vms);
	}

}
