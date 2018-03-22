package com.ncc.savior.virtueadmin.data.jpa;

import org.springframework.data.repository.CrudRepository;

import com.ncc.savior.virtueadmin.model.VirtueUser;

/**
 * JPA repository class that stores {@link VirtueUser}s.
 */
public interface UserRepository extends CrudRepository<VirtueUser, String> {

}
