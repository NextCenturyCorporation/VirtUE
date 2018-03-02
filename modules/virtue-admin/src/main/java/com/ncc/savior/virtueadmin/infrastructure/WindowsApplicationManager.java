package com.ncc.savior.virtueadmin.infrastructure;

import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.OS;
import com.ncc.savior.virtueadmin.model.AbstractVirtualMachine;

public class WindowsApplicationManager implements IApplicationManager {

	@Override
	public void startApplicationOnVm(AbstractVirtualMachine vm, ApplicationDefinition application, int maxTries) {
		if (vm.getOs() != OS.WINDOWS) {
			throw new IllegalArgumentException("vm OS must be Windows, but was: " + vm.getOs());
		}
		
	}

}
