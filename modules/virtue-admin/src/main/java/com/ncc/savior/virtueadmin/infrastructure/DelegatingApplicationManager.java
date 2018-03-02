package com.ncc.savior.virtueadmin.infrastructure;

import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.AbstractVirtualMachine;

/**
 * Delegates application operations to an OS-specific manager.
 * 
 * @author clong
 *
 */
public class DelegatingApplicationManager implements IApplicationManager {

	
	@Override
	public void startApplicationOnVm(AbstractVirtualMachine vm, ApplicationDefinition application, int maxTries) {
		// TODO Auto-generated method stub

	}

}
