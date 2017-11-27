package com.ncc.savior.virtueadmin.infrastructure;

/**
 * Interface for working with different backend infrastructure models and
 * services.
 * 
 *
 */
public interface IInfrastructureService {

	Object provisionRole(String roleId, boolean useAlreadyProvisioned);

}
