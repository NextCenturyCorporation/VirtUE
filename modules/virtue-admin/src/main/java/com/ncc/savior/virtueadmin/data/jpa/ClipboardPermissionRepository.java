package com.ncc.savior.virtueadmin.data.jpa;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.ncc.savior.virtueadmin.model.ClipboardPermission;

public interface ClipboardPermissionRepository extends CrudRepository<ClipboardPermission, String> {

	ClipboardPermission findBySourceGroupIdAndDestinationGroupId(String sourceGroupId, String destinationGroupId);

	List<ClipboardPermission> findBySourceGroupId(String sourceGroupId);

}
