package com.ncc.savior.virtueadmin.data.jpa;

import java.util.Iterator;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;

import com.ncc.savior.virtueadmin.data.IUserManager;
import com.ncc.savior.virtueadmin.model.VirtueUser;
import com.ncc.savior.virtueadmin.util.SaviorException;

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
			throw new SaviorException(SaviorException.USER_NOT_FOUND, "User=" + username + " was not found");
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

}
