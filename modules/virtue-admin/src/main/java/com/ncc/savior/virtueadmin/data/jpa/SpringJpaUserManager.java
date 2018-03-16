package com.ncc.savior.virtueadmin.data.jpa;

import java.util.Iterator;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;

import com.ncc.savior.virtueadmin.data.IUserManager;
import com.ncc.savior.virtueadmin.model.persistance.JpaVirtueUser;

/**
 * {@link IUserManager} that uses Spring and JPA.
 */
public class SpringJpaUserManager implements IUserManager {

	@Autowired
	private UserRepository userRepo;

	@Override
	public void addUser(JpaVirtueUser user) {
		userRepo.save(user);
	}

	@Override
	public JpaVirtueUser getUser(String username) {
		Optional<JpaVirtueUser> user = userRepo.findById(username);
		return (user.orElse(null));
	}

	@Override
	public Iterable<JpaVirtueUser> getAllUsers() {
		return userRepo.findAll();
	}

	@Override
	public void clear() {
		Iterator<JpaVirtueUser> itr = userRepo.findAll().iterator();
		while (itr.hasNext()) {
			JpaVirtueUser user = itr.next();
			user.removeAllVirtueTemplates();
			userRepo.save(user);
		}
		userRepo.deleteAll();
	}

	@Override
	public void removeUser(JpaVirtueUser user) {
		userRepo.delete(user);
	}

	@Override
	public void removeUser(String usernameToRemove) {
		userRepo.deleteById(usernameToRemove);
	}

}
