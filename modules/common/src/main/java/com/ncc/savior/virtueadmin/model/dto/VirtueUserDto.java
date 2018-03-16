package com.ncc.savior.virtueadmin.model.dto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import com.ncc.savior.virtueadmin.model.BaseVirtueUser;
import com.ncc.savior.virtueadmin.model.persistance.JpaVirtueUser;

/**
 * Class to represent a user that has been authenticated by the security
 * service. What we want in here is TDB.
 * 
 *
 */
public class VirtueUserDto extends BaseVirtueUser {

	private Collection<String> virtueTemplateIds;

	protected VirtueUserDto() {

	}

	public VirtueUserDto(String name, Collection<String> authorities) {
		super(name, authorities);
		this.virtueTemplateIds = new HashSet<String>();
	}

	public VirtueUserDto(BaseVirtueUser user, Collection<String> virtueTemplateIds) {
		super(user.getUsername(), user.getAuthorities());
		this.virtueTemplateIds = virtueTemplateIds;
	}

	public Collection<String> getVirtueTemplateIds() {
		return virtueTemplateIds;
	}

	public void addVirtueTemplateId(String virtueTemplateId) {
		virtueTemplateIds.add(virtueTemplateId);
	}

	public void removeVirtueTemplate(String virtueTemplateId) {
		virtueTemplateIds.remove(virtueTemplateId);
	}


	@Override
	public String toString() {
		return "RestVirtueUser [virtueTemplateIds=" + virtueTemplateIds + ", username=" + username + ", authorities="
				+ authorities + "]";
	}

	public void removeAllVirtueTemplates() {
		virtueTemplateIds.clear();
	}

	public static JpaVirtueUser anonymousUser() {
		return new JpaVirtueUser("anonymous", new ArrayList<String>());
	}

}
