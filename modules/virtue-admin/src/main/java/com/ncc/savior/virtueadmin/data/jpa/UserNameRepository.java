package com.ncc.savior.virtueadmin.data.jpa;

import org.springframework.data.repository.CrudRepository;

import com.ncc.savior.virtueadmin.model.UserName;

public interface UserNameRepository extends CrudRepository<UserName, String> {

}
