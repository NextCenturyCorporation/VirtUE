package com.ncc.savior.virtueadmin.infrastructure;

import com.ncc.savior.virtueadmin.model.VmState;

public interface IStateUpdateListener {
	void updateVmState(String vmId, VmState state);
}
