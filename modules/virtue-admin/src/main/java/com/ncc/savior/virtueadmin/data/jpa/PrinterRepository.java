package com.ncc.savior.virtueadmin.data.jpa;

import org.springframework.data.repository.CrudRepository;

import com.ncc.savior.virtueadmin.model.Printer;

/**
 * JPA Repository that stores {@link Printer}s.
 */
public interface PrinterRepository extends CrudRepository<Printer, String> {

}
