package com.ncc.savior.virtueadmin.infrastructure.mixed;

import java.util.Collection;

import com.ncc.savior.virtueadmin.model.VirtualMachineTemplate;

public interface IXenVmCreationScheduleManager {

	Collection<VirtualMachineTemplate> getTemplates(String id);

	void addTemplates(String id, Collection<VirtualMachineTemplate> linuxVmts);

}
