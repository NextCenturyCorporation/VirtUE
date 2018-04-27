package com.ncc.savior.virtueadmin.data.jpa;

import org.springframework.data.repository.CrudRepository;

import com.ncc.savior.virtueadmin.model.ApplicationDefinition;

/**
 * JPA Repository that stores {@link ApplicationDefinition}s.
 */
public interface ApplicationDefinitionRepository extends CrudRepository<ApplicationDefinition, String> {

}
