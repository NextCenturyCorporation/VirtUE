package com.ncc.savior.virtueadmin.infrastructure;

import java.util.Map;

import com.ncc.savior.virtueadmin.model.AbstractVirtualMachine;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.OS;
import com.ncc.savior.virtueadmin.model.desktop.IApplicationInstance;

/**
 * Delegates application operations to an OS-specific manager.
 * 
 * @author clong
 *
 */
public class DelegatingApplicationManager implements IApplicationManager {

	private Map<OS, IApplicationManager> delegates;
	public DelegatingApplicationManager(Map<OS, IApplicationManager> applicationManagerDelegates) {
		this.delegates = applicationManagerDelegates;
	}
	
	@Override
	public IApplicationInstance startApplicationOnVm(AbstractVirtualMachine vm, ApplicationDefinition application, int maxTries) {
		// TODO Auto-generated method stub
		return null;
	}

}
