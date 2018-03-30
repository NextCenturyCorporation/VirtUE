package com.ncc.savior.virtueadmin.infrastructure;

import java.io.File;

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

	/**
	 * Retrieves a file that the key is already stored in.
	 * 
	 * @param privateKey
	 * @return
	 */
	File getKeyFileByName(String privateKey);

}
