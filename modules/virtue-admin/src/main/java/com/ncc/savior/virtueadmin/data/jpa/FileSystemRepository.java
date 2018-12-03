package com.ncc.savior.virtueadmin.data.jpa;

import org.springframework.data.repository.CrudRepository;

import com.ncc.savior.virtueadmin.model.FileSystem;

/**
 * JPA Repository that stores {@link FileSystem}s.
 */
public interface FileSystemRepository extends CrudRepository<FileSystem, String> {

}
