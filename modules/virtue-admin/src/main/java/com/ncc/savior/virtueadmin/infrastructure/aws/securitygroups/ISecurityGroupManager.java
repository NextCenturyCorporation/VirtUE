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