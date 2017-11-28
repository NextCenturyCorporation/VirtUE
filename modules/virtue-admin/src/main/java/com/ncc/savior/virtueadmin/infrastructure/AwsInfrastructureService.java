package com.ncc.savior.virtueadmin.infrastructure;

import com.ncc.savior.virtueadmin.model.User;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VirtualMachineTemplate;
import com.ncc.savior.virtueadmin.model.VirtueInstance;
import com.ncc.savior.virtueadmin.model.VirtueTemplate;
import com.ncc.savior.virtueadmin.service.VirtueUserService.StateUpdateListener;

/**
 * Infastructure service for AWS.
 * 
 *
 */
public class AwsInfrastructureService implements IInfrastructureService {


	@Override
	public boolean launchVirtue(VirtueInstance virtue) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public VirtueInstance provisionTemplate(User user, VirtueTemplate template, boolean useAlreadyProvisioned) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean stopVirtue(VirtueInstance virtue) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean destroyVirtue(VirtueInstance virtue) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public VirtualMachine provisionVm(VirtualMachineTemplate vmTemplate) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addStateUpdateListener(StateUpdateListener stateUpdateListener) {
		// TODO Auto-generated method stub

	}
}
