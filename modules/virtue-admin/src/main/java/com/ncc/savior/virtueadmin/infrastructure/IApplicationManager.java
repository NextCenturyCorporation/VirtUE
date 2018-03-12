package com.ncc.savior.virtueadmin.infrastructure;

import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.desktop.IApplicationInstance;
import com.ncc.savior.virtueadmin.model.AbstractVirtualMachine;

/**
 * Starts and manages applications running on VMs.
 * 
 *
 */
public interface IApplicationManager {
	/**
	 * Initiates an application on a given VM.
	 * 
	 * @param vm
	 * @param application
	 * @return 
	 */
	IApplicationInstance startApplicationOnVm(AbstractVirtualMachine vm, ApplicationDefinition application, int maxTries);
}
