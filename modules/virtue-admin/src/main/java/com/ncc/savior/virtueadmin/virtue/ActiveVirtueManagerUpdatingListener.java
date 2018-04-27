package com.ncc.savior.virtueadmin.virtue;

import java.util.Collection;

import com.ncc.savior.virtueadmin.data.IActiveVirtueDao;
import com.ncc.savior.virtueadmin.infrastructure.IUpdateListener;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VirtueInstance;

public class ActiveVirtueManagerUpdatingListener implements IUpdateListener<VirtualMachine> {
	private IActiveVirtueDao virtueManager;

	public ActiveVirtueManagerUpdatingListener(IActiveVirtueDao virtueManager) {
		this.virtueManager = virtueManager;
	}

	@Override
	public void updateElements(Collection<VirtualMachine> vms) {
		virtueManager.updateVms(vms);
	}

	public void updateVirtue(VirtueInstance virtue) {
		virtueManager.updateVirtue(virtue);
	}

}
