package com.ncc.savior.virtueadmin.infrastructure.aws.subnet;

import java.util.Map;

/**
 * Implementations will provide subnetID and VPC ID for the system. Multiple
 * subnets may be used based on the given key (usually virtue id). If no subnet
 * has been assigned to a key, a new one should be created and assigned or an
 * exception throw.
 *
 */
public interface IVpcSubnetProvider {

	String TAG_USERNAME = "username";
	String TAG_VIRTUE_NAME = "virtue-name";
	String TAG_VIRTUE_ID = "virtue-id";
	String TAG_NAME = "Name";

	/**
	 * Returns subnet for the given key. Keys typically should be the same for a
	 * virtue. Unfortunately, since this method may be called prior to virtue
	 * creation, there may not be a virtue ID yet.
	 * 
	 * @param subnetKey
	 * @return
	 */
	String getSubnetId(String subnetKey, Map<String, String> tags);

	/**
	 * Tells the provider that the given subnet is no longer in use. The
	 * implementation then may decide whether to retain the subnet for later use or
	 * remove it from the infrastructure. Call MUST be sure that the subnet is
	 * actually no longer in use.
	 * 
	 * @param subnetId
	 */
	void releaseBySubnetKey(String id);

	/**
	 * Returns the system VPC ID.
	 * 
	 * @return
	 */
	String getVpcId();

	

}
