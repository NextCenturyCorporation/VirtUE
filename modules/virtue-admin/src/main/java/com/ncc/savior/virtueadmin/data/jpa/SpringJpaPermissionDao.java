package com.ncc.savior.virtueadmin.data.jpa;

import org.springframework.beans.factory.annotation.Autowired;

import com.ncc.savior.virtueadmin.model.ClipboardPermission;
import com.ncc.savior.virtueadmin.model.ClipboardPermissionOption;

public class SpringJpaPermissionDao implements IPermissionDao {
	@Autowired
	private ClipboardPermissionRepository clipboardPermissionRepository;


	@Override
	public ClipboardPermission getClipboardPermission(String sourceId, String destId) {
		ClipboardPermission permission = clipboardPermissionRepository
				.findBySourceGroupIdAndDestinationGroupId(sourceId, destId);
		return permission;
	}

	@Override
	public void setClipboardPermission(String sourceId, String destinationId, ClipboardPermissionOption option) {
		ClipboardPermission permission = new ClipboardPermission(sourceId, destinationId, option);
		clipboardPermissionRepository.save(permission);

	}
}
