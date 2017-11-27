package com.ncc.savior.virtueadmin.infrastructure;

import com.ncc.savior.virtueadmin.model.User;
import com.ncc.savior.virtueadmin.model.VirtueInstance;
import com.ncc.savior.virtueadmin.model.VirtueTemplate;

/**
 * Interface for working with different backend infrastructure models and
 * services.
 * 
 *
 */
public interface IInfrastructureService {

	VirtueInstance provisionTemplate(User user, VirtueTemplate template, boolean useAlreadyProvisioned);

	boolean launchVirtue(VirtueInstance virtue);

	boolean stopVirtue(VirtueInstance virtue);

	boolean destroyVirtue(VirtueInstance virtue);

}
