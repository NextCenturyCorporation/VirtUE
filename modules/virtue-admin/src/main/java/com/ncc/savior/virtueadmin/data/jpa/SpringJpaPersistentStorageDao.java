/*
 * Copyright (C) 2019 Next Century Corporation
 * 
 * This file may be redistributed and/or modified under either the GPL
 * 2.0 or 3-Clause BSD license. In addition, the U.S. Government is
 * granted government purpose rights. For details, see the COPYRIGHT.TXT
 * file at the root of this project.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 * 
 * SPDX-License-Identifier: (GPL-2.0-only OR BSD-3-Clause)
 */
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
