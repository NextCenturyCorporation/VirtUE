package com.ncc.savior.virtueadmin.data.jpa;

import org.springframework.data.repository.CrudRepository;

import persistance.JpaVirtueUser;

/**
 * JPA repository class that stores {@link VirtueUser}s.
 */
public interface UserRepository extends CrudRepository<JpaVirtueUser, String> {

}
