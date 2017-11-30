package com.ncc.savior.virtueadmin.data;

import java.util.Map;
import java.util.Set;

import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VirtueInstance;
import com.ncc.savior.virtueadmin.model.VmState;

public interface IActiveVirtueDao {

	Map<String, Set<VirtueInstance>> getVirtuesFromTemplateIds(Set<String> templateIds);

	void updateVmState(String vmId, VmState state);

	VirtualMachine getVmWithApplication(String virtueId, String applicationId);

	void addVirtue(VirtueInstance vi);

}
