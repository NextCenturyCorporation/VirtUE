package com.ncc.savior.virtueadmin.infrastructure;

/**
 * Interface for a manager to map key names to private keys for SSH access.
 */
public interface IKeyManager {

	/**
	 * Retrieves a key by a name. The key will be returned as a string.
	 * 
	 * @param keyName
	 * @return
	 */
	String getKeyByName(String keyName);

}
