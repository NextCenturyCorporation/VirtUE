package com.ncc.savior.virtueadmin.infrastructure;

import com.ncc.savior.virtueadmin.model.User;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VirtueInstance;
import com.ncc.savior.virtueadmin.model.VirtueTemplate;
import com.ncc.savior.virtueadmin.service.IStateUpdateListener;

/**
 * Interface for working with different backend infrastructure models and
 * services.
 * 
 *
 */
public interface IInfrastructureService {
	void addStateUpdateListener(IStateUpdateListener stateUpdateListener);

	/**
	 * Get a provisioned instance of a template. The provisioned instance could come
	 * from a pre-provisioned pool or created.
	 * 
	 * @param user
	 * 
	 * @param template
	 * @return
	 */
	VirtueInstance getProvisionedVirtueFromTemplate(User user, VirtueTemplate template);

	VirtualMachine startVm(VirtualMachine vm);

}
