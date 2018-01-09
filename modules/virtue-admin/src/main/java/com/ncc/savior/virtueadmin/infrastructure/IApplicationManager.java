package com.ncc.savior.virtueadmin.infrastructure;

import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.VirtualMachine;

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
	 */
	void startApplicationOnVm(VirtualMachine vm, ApplicationDefinition application, int maxTries);
}
