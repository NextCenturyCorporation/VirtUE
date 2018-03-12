package com.ncc.savior.virtueadmin.data.jpa;

import org.springframework.data.repository.CrudRepository;

import com.ncc.savior.virtueadmin.model.VirtualMachine;

/**
 * JPA repository that stores {@link VirtualMachine}s.
 */
public interface VirtualMachineRepository extends CrudRepository<VirtualMachine, String> {

}
