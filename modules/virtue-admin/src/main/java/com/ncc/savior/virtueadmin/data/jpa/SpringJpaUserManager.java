package com.ncc.savior.virtueadmin.data.jpa;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;

import com.ncc.savior.virtueadmin.data.IUserManager;
import com.ncc.savior.virtueadmin.model.VirtueUser;

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
		return (user.orElse(null));
	}
	
	@Override
	public Iterable<VirtueUser> getAllUsers()
	{
		return userRepo.findAll();
	}

	@Override
	public void clear() {
		userRepo.deleteAll();
	}

	@Override
	public void removeUser(VirtueUser user) {
		userRepo.delete(user);
	}

}
