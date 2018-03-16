package com.ncc.savior.virtueadmin.data.jpa;

import org.springframework.data.repository.CrudRepository;

import persistance.JpaVirtueTemplate;

/**
 * JPA respository that stores {@link VirtueTemplate}s.
 */
public interface VirtueTemplateRepository extends CrudRepository<JpaVirtueTemplate, String> {

}
