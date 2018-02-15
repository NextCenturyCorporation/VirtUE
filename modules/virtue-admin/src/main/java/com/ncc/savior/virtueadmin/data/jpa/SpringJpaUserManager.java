package com.ncc.savior.virtueadmin.data.jpa;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;

import com.ncc.savior.virtueadmin.data.IUserManager;
import com.ncc.savior.virtueadmin.model.User;

public class SpringJpaUserManager implements IUserManager {

	@Autowired
	private UserRepository userRepo;

	@Override
	public void addUser(User user) {
		userRepo.save(user);
	}

	@Override
	public User getUser(String username) {
		Optional<User> user = userRepo.findById(username);
		return (user.orElse(null));
	}
	
	@Override
	public Iterable<User> getAllUsers()
	{
		return userRepo.findAll();
	}

	@Override
	public void clear() {
		userRepo.deleteAll();
	}

	@Override
	public void removeUser(User user) {
		userRepo.delete(user);
	}

}
