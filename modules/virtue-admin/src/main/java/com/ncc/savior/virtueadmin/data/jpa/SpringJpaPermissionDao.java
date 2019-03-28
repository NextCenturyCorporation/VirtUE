/*
 * Copyright (C) 2019 Next Century Corporation
 * 
 * This file may be redistributed and/or modified under either the GPL
 * 2.0 or 3-Clause BSD license. In addition, the U.S. Government is
 * granted government purpose rights. For details, see the COPYRIGHT.TXT
 * file at the root of this project.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 * 
 * SPDX-License-Identifier: (GPL-2.0-only OR BSD-3-Clause)
 */
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
