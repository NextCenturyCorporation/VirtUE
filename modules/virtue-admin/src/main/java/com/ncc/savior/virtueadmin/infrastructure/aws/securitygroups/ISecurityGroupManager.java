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
package com.ncc.savior.virtueadmin.infrastructure.aws.securitygroups;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.ncc.savior.virtueadmin.model.SecurityGroupPermission;

/**
 * Manages creation, removal of security groups as well as authorization and
 * Revocation of permissions for security groups.
 *
 */
public interface ISecurityGroupManager {

	/**
	 * When ALL template IDs in the system are passed into this method, it will
	 * delete any security groups that match this server and are not in the list.
	 * This will delete only and not creating missing. This method is mainly used to
	 * cleanup extra entries that didn't get deleted in a normal fashion and is
	 * typically needed in development.
	 * 
	 * @param allTemplateIds
	 */
	void sync(Collection<String> allTemplateIds);

	List<String> getAllSecurityGroupIds();

	/**
	 * Returns all {@link SecurityGroupPermission} by each Security Group ID.
	 * 
	 * @return
	 */
	Map<String, Collection<SecurityGroupPermission>> getAllSecurityGroupPermissions();

	Collection<SecurityGroupPermission> getSecurityGroupPermissionsByGroupId(String groupId);

	Collection<SecurityGroupPermission> getSecurityGroupPermissionsByTemplateId(String templateId);

	/**
	 * Returns the already existing security group id. If non exist, create a new
	 * one. Should never return null.
	 * 
	 * @param templateId
	 * @return
	 */
	String getSecurityGroupIdByTemplateId(String templateId);

	void authorizeSecurityGroup(String groupId, SecurityGroupPermission permission);

	void revokeSecurityGroup(String groupId, SecurityGroupPermission permission);

	void removeSecurityGroup(String groupId);

}