package com.ncc.savior.virtueadmin.infrastructure.mixed;

public interface IVpcSubnetProvider {

	/**
	 * Returns subnet for the given key. Keys typically should be the same for a
	 * virtue. Unfortunately, since this method may be called prior to virtue
	 * creation, there may not be a virtue ID yet.
	 * 
	 * @param subnetKey
	 * @return
	 */
	String getSubnetId(String subnetKey);

	/**
	 * Returns the system VPC ID.
	 * 
	 * @return
	 */
	String getVpcId();

}
