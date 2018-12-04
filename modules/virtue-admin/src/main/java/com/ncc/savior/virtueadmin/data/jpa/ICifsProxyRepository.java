package com.ncc.savior.virtueadmin.data.jpa;

import org.springframework.data.repository.CrudRepository;

import com.ncc.savior.virtueadmin.model.CifsProxyData;

/**
 * JPA Repository for {@link CifsProxyData}
 */
public interface ICifsProxyRepository extends CrudRepository<CifsProxyData, String> {

}