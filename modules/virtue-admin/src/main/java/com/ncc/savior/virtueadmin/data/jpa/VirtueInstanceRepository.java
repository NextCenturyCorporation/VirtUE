package com.ncc.savior.virtueadmin.data.jpa;

import java.util.Collection;
import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.ncc.savior.virtueadmin.model.BaseVirtueInstance;

import persistance.JpaVirtueInstance;

/**
 * JPA respository that stores {@link BaseVirtueInstance}s. This respository
 * contains extra methods in addition to the default Spring JPA methods.
 */
public interface VirtueInstanceRepository extends CrudRepository<JpaVirtueInstance, String> {

	List<JpaVirtueInstance> findByUsernameAndTemplateIdIn(String username, Collection<String> templateIds);

	Collection<JpaVirtueInstance> findByUsername(String username);

	JpaVirtueInstance findByUsernameAndId(String username, String instanceId);

}
