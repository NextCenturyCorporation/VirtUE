package com.ncc.savior.virtueadmin.data.jpa;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.ncc.savior.virtueadmin.model.ClipboardPermission;
import com.ncc.savior.virtueadmin.model.ClipboardPermissionOption;

/**
 * Spring JPA implementation of {@link IPermissionDao}, using
 * {@link ClipboardPermissionRepository}.
 * 
 *
 */
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

	@Override
	public List<ClipboardPermission> getClipboardPermissionForSource(String sourceId) {
		List<ClipboardPermission> list = clipboardPermissionRepository.findBySourceGroupId(sourceId);
		return list;
	}

	@Override
	public List<ClipboardPermission> getClipboardPermissionForDestination(String destinationId) {
		List<ClipboardPermission> list = clipboardPermissionRepository.findByDestinationGroupId(destinationId);
		return list;
	}

	@Override
	public Iterable<ClipboardPermission> getAllClipboardPermissions() {
		return clipboardPermissionRepository.findAll();
	}

	@Override
	public void clearPermission(String sourceId, String destId) {
		ClipboardPermission entity = new ClipboardPermission(sourceId, destId, null);
		clipboardPermissionRepository.delete(entity);
	}
}
