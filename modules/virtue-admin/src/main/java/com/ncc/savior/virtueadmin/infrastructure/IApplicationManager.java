package com.ncc.savior.virtueadmin.infrastructure;

import java.io.File;
import java.io.IOException;

import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.persistance.JpaVirtualMachine;

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
	void startApplicationOnVm(JpaVirtualMachine vm, ApplicationDefinition application, int maxTries);

	/**
	 * Tries to get a started Xpra server. If none is found, it will attempt to
	 * start one and immediately test for it.
	 * 
	 * -1 means no server
	 * 
	 * throwing means failure.
	 * 
	 */
	int startOrGetXpraServer(JpaVirtualMachine vm, File privateKeyFile) throws IOException;
}
