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

import java.util.Iterator;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;

import com.ncc.savior.util.SaviorErrorCode;
import com.ncc.savior.util.SaviorException;
import com.ncc.savior.virtueadmin.data.IUserManager;
import com.ncc.savior.virtueadmin.model.VirtueUser;

/**
 * {@link IUserManager} that uses Spring and JPA.
 */
public class SpringJpaUserManager implements IUserManager {

	@Autowired
	private UserRepository userRepo;

	@Override
	public void addUser(VirtueUser user) {
		userRepo.save(user);
	}

	@Override
	public VirtueUser getUser(String username) {
		Optional<VirtueUser> user = userRepo.findById(username);
		if (user.isPresent()) {
			return user.get();
		} else {
			throw new SaviorException(SaviorErrorCode.USER_NOT_FOUND, "User=" + username + " was not found");
		}
	}

	@Override
	public Iterable<VirtueUser> getAllUsers() {
		return userRepo.findAll();
	}

	@Override
	public void clear(boolean removeUsers) {
		Iterator<VirtueUser> itr = userRepo.findAll().iterator();
		while (itr.hasNext()) {
			VirtueUser user = itr.next();
			user.removeAllVirtueTemplates();
			userRepo.save(user);
		}
		itr = userRepo.findAll().iterator();
		if (removeUsers) {
			userRepo.deleteAll();
		}
	}

	@Override
	public void removeUser(VirtueUser user) {
		userRepo.delete(user);
	}

	@Override
	public void removeUser(String usernameToRemove) {
		userRepo.deleteById(usernameToRemove);
	}

	@Override
	public boolean userExists(String username) {
		return userRepo.existsById(username);
	}

	@Override
	public void enableDisableUser(String username, Boolean enable) {
		Optional<VirtueUser> userContainer = userRepo.findById(username);
		if (userContainer.isPresent()) {
			VirtueUser user = userContainer.get();
			user.setEnabled(enable);
			userRepo.save(user);
		} else {
			throw new SaviorException(SaviorErrorCode.USER_NOT_FOUND, "User=" + username + " not found");
		}
	}

}
