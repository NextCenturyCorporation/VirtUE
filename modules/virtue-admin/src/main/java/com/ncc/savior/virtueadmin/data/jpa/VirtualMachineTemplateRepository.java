package com.ncc.savior.virtueadmin.data.jpa;

import org.springframework.data.repository.CrudRepository;

import com.ncc.savior.virtueadmin.model.VirtualMachineTemplate;

/**
 * JPA respository that stores {@link VirtualMachineTemplate}s.
 */
public interface VirtualMachineTemplateRepository extends CrudRepository<VirtualMachineTemplate, String> {

}
