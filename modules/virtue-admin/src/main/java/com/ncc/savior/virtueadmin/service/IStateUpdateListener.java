package com.ncc.savior.virtueadmin.service;

import com.ncc.savior.virtueadmin.model.VirtueState;
import com.ncc.savior.virtueadmin.model.VmState;

public interface IStateUpdateListener {

	void updateVmState(String virtueId, String vmId, VmState state);

	void updateVirtueState(String id, VirtueState state);

}
