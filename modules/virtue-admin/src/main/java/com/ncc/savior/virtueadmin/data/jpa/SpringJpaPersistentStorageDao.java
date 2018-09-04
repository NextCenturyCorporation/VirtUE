package com.ncc.savior.virtueadmin.data.jpa;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;

import com.ncc.savior.virtueadmin.data.IPersistentStorageDao;
import com.ncc.savior.virtueadmin.model.VirtuePersistentStorage;
import com.ncc.savior.virtueadmin.model.VirtuePersistentStorage.VirtuePersistentStorageId;

/**
 * Implementation of {@link IPermissionDao} that uses spring JPA. This handles
 * the database storage for {@link VirtuePersistentStorage}.
 */
public class SpringJpaPersistentStorageDao implements IPersistentStorageDao {
	@Autowired
	private PersistentStorageRepository persistentStorageRepository;

	@Override
	public Iterable<VirtuePersistentStorage> getAllPersistentStorage() {
		return persistentStorageRepository.findAll();
	}

	/**
	 * Get {@link VirtuePersistentStorage} and return null if not found.
	 */
	@Override
	public VirtuePersistentStorage getPersistentStorage(String username, String virtueTemplateId) {
		// VirtuePersistentStorage s =
		// persistentStorageRepository.findByUsernameAndVirtueTemplateId(username,
		// virtueTemplateId);
		VirtuePersistentStorageId id = new VirtuePersistentStorageId();
		id.setUsername(username);
		id.setVirtueTemplateId(virtueTemplateId);
		Optional<VirtuePersistentStorage> s = persistentStorageRepository.findById(id);
		return (s.isPresent() ? s.get() : null);
	}

	@Override
	public void deletePersistentStorage(VirtuePersistentStorage ps) {
		persistentStorageRepository.delete(ps);
	}

	@Override
	public void savePersistentStorage(VirtuePersistentStorage newPs) {
		persistentStorageRepository.save(newPs);
	}

	/**
	 * Get all persistent storage for the given user.
	 */
	@Override
	public Iterable<VirtuePersistentStorage> getPersistentStorageForUser(String username) {
		return persistentStorageRepository.findByUsername(username);
	}
}
