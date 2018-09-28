package com.ncc.savior.virtueadmin.data.jpa;

import org.springframework.data.repository.CrudRepository;

import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VirtuePersistentStorage;
import com.ncc.savior.virtueadmin.model.VirtuePersistentStorage.VirtuePersistentStorageId;

/**
 * JPA repository that stores {@link VirtualMachine}s.
 */
public interface PersistentStorageRepository
		extends CrudRepository<VirtuePersistentStorage, VirtuePersistentStorageId> {

	Iterable<VirtuePersistentStorage> findByUsername(String username);
	// VirtuePersistentStorage findByUsernameAndVirtueTemplateId(String username,
	// String virtueTemplateId);
}
