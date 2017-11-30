package com.ncc.savior.virtueadmin.infrastructure;

import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.VirtualMachine;

public interface IApplicationManager {
	void startApplicationOnVm(VirtualMachine vm, ApplicationDefinition application);
}
