package com.ncc.savior.virtueadmin.data.jpa;

import org.springframework.data.repository.CrudRepository;

import com.ncc.savior.virtueadmin.model.persistance.JpaVirtualMachineTemplate;

/**
 * JPA respository that stores {@link VirtualMachineTemplate}s.
 */
public interface VirtualMachineTemplateRepository extends CrudRepository<JpaVirtualMachineTemplate, String> {

}
