package com.ncc.savior.virtueadmin.infrastructure;

import com.ncc.savior.virtueadmin.model.User;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VirtueInstance;
import com.ncc.savior.virtueadmin.model.VirtueTemplate;
import com.ncc.savior.virtueadmin.service.IStateUpdateListener;

/**
 * Infastructure service for AWS.
 * 
 *
 */
public class AwsInfrastructureService implements IInfrastructureService {

	@Override
	public void addStateUpdateListener(IStateUpdateListener stateUpdateListener) {
		// TODO Auto-generated method stub

	}

	@Override
	public VirtueInstance getProvisionedVirtueFromTemplate(User user, VirtueTemplate template) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public VirtualMachine startVm(VirtualMachine vm) {
		// TODO Auto-generated method stub
		return null;
	}
}
