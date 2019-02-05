package com.ncc.savior.virtueadmin.data.jpa;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.ncc.savior.virtueadmin.model.CifsShareCreationParameter;

public interface ICifsShareRepository extends CrudRepository<CifsShareCreationParameter, String> {

	List<CifsShareCreationParameter> findByVirtueId(String virtueId);

}
