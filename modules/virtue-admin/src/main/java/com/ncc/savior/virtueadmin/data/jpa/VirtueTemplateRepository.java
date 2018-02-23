package com.ncc.savior.virtueadmin.data.jpa;

import java.util.Collection;

import org.springframework.data.repository.CrudRepository;

import com.ncc.savior.virtueadmin.model.VirtueUser;
import com.ncc.savior.virtueadmin.model.VirtueTemplate;

public interface VirtueTemplateRepository extends CrudRepository<VirtueTemplate, String> {

	Collection<VirtueTemplate> findByUsers(VirtueUser user);

	VirtueTemplate findByUsersAndId(VirtueUser user, String templateId);
}
