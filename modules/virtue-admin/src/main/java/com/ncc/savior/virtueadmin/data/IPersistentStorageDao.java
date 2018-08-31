package com.ncc.savior.virtueadmin.data;

import com.ncc.savior.virtueadmin.model.VirtuePersistentStorage;

/**
 * Data Access Object for persistent storage volumes.
 * 
 *
 */
public interface IPersistentStorageDao {

	Iterable<VirtuePersistentStorage> getAllPersistentStorage();

	/**
	 * Returns value or null
	 * 
	 * @param virtueTemplateId
	 * @param userName
	 * @return
	 */
	VirtuePersistentStorage getPersistentStorage(String virtueTemplateId, String userName);

	void deletePersistentStorage(VirtuePersistentStorage ps);

	void savePersistentStorage(VirtuePersistentStorage newPs);

	Iterable<VirtuePersistentStorage> getPersistentStorageForUser(String username);

}
