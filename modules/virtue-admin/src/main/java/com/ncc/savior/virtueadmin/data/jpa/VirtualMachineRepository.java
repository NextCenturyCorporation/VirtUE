package com.ncc.savior.virtueadmin.data.jpa;

import org.springframework.data.repository.CrudRepository;

import com.ncc.savior.virtueadmin.model.VirtualMachine;

public interface VirtualMachineRepository extends CrudRepository<VirtualMachine, String> {

}
