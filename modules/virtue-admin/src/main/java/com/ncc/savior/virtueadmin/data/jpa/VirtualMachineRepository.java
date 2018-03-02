package com.ncc.savior.virtueadmin.data.jpa;

import org.springframework.data.repository.CrudRepository;

import com.ncc.savior.virtueadmin.model.AbstractVirtualMachine;

public interface VirtualMachineRepository extends CrudRepository<AbstractVirtualMachine, String> {

}
