package com.ncc.savior.virtueadmin.data.jpa;

import java.util.Collection;
import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.ncc.savior.virtueadmin.model.VirtueInstance;

public interface VirtueInstanceRepository extends CrudRepository<VirtueInstance, String> {

	List<VirtueInstance> findByUsernameAndTemplateIdIn(String username, Collection<String> templateIds);

	Collection<VirtueInstance> findByUsername(String username);

	VirtueInstance findByUsernameAndId(String username, String instanceId);

}
