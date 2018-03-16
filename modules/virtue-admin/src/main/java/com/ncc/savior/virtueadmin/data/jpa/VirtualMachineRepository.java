package com.ncc.savior.virtueadmin.data.jpa;

import org.springframework.data.repository.CrudRepository;

import net.bytebuddy.agent.VirtualMachine;
import persistance.JpaVirtualMachine;

/**
 * JPA repository that stores {@link VirtualMachine}s.
 */
public interface VirtualMachineRepository extends CrudRepository<JpaVirtualMachine, String> {

}
