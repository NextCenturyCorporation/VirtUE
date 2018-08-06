package com.ncc.savior.virtueadmin.data.jpa;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.ncc.savior.virtueadmin.model.ClipboardPermission;

/**
 * JPA {@link CrudRepository} for retreiving and storing
 * {@link ClipboardPermission}s.
 * 
 *
 */
public interface ClipboardPermissionRepository extends CrudRepository<ClipboardPermission, String> {

	ClipboardPermission findBySourceGroupIdAndDestinationGroupId(String sourceGroupId, String destinationGroupId);

	List<ClipboardPermission> findBySourceGroupId(String sourceGroupId);

	List<ClipboardPermission> findByDestinationGroupId(String destinationId);

}
