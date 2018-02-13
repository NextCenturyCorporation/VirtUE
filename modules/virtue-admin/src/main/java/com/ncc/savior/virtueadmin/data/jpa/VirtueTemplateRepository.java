package com.ncc.savior.virtueadmin.data.jpa;

import java.util.Collection;

import org.springframework.data.repository.CrudRepository;

import com.ncc.savior.virtueadmin.model.User;
import com.ncc.savior.virtueadmin.model.VirtueTemplate;

public interface VirtueTemplateRepository extends CrudRepository<VirtueTemplate, String> {

	Collection<VirtueTemplate> findByUsers(User user);

	VirtueTemplate findByUsersAndId(User user, String templateId);
}
