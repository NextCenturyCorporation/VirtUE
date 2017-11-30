package com.ncc.savior.virtueadmin.virtue;

import java.util.Map;
import java.util.Set;

import com.ncc.savior.virtueadmin.model.User;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VirtueInstance;
import com.ncc.savior.virtueadmin.model.VirtueTemplate;

public interface IActiveVirtueManager {

	Map<String, Set<VirtueInstance>> getVirtuesFromTemplateIds(Set<String> keySet);

	VirtualMachine getVmWithApplication(String virtueId, String applicationId);

	VirtualMachine startVirtualMachine(VirtualMachine vm);

	VirtueInstance provisionTemplate(User user, VirtueTemplate template);

}
