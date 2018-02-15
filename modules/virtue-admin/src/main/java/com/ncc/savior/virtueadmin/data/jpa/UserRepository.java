package com.ncc.savior.virtueadmin.data.jpa;

import org.springframework.data.repository.CrudRepository;

import com.ncc.savior.virtueadmin.model.VirtueUser;

public interface UserRepository extends CrudRepository<VirtueUser, String> {

}
