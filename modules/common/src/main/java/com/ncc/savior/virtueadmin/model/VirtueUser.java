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
package com.ncc.savior.virtueadmin.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Class to represent a user that has been authenticated by the security
 * service. What we want in here is TDB.
 * 
 *
 */
@Entity
@Schema(description = "User representation for the Savior system.")
public class VirtueUser {
	private static final VirtueUser testUser;
	private static final VirtueUser anonymousUser;
	private static VirtueUser adminUser;

	public static final String ROLE_ADMIN = "ROLE_ADMIN";
	public static final String ROLE_USER = "ROLE_USER";

	@Id
	@Schema(description = "Username for the user.")
	private String username;
	@ElementCollection(targetClass = String.class)
	@Schema(description = "Authorities or groups that the user is assigned to.  These authorities ties to access to the savior system and not access to certain virtues.")
	private Collection<String> authorities;
	@ManyToMany()
	private Collection<VirtueTemplate> virtueTemplates;
	@Transient
	@Schema(description = "List of IDs of virtue templates for which the user has access to and can provision.")
	private Collection<String> virtueTemplateIds;
	@Schema(description = "Boolean to determine whether the user is enabled or not. Disabled users should not be able to login.")
	private boolean enabled;

	static {
		testUser = new VirtueUser("testUser", new ArrayList<String>(), true);
		anonymousUser = new VirtueUser("anonymous", new ArrayList<String>(), true);
		ArrayList<String> adminAuths = new ArrayList<String>();
		adminAuths.add(ROLE_ADMIN);
		adminAuths.add(ROLE_USER);
		adminUser = new VirtueUser("admin", adminAuths, true);
	}

	protected VirtueUser() {

	}

	public VirtueUser(String name, Collection<String> authorities, boolean enabled) {
		this.username = name;
		this.authorities = authorities;
		this.virtueTemplates = new HashSet<VirtueTemplate>();
		this.enabled = enabled;
	}

	public static VirtueUser testUser() {
		return testUser;
	}

	public String getUsername() {
		return username;
	}

	public Collection<String> getAuthorities() {
		return authorities;
	}

	public void setVirtueTemplates(Collection<VirtueTemplate> myVts) {
		this.virtueTemplates = myVts;
	}

	@JsonIgnore
	public Collection<VirtueTemplate> getVirtueTemplates() {
		return virtueTemplates;
	}

	public void addVirtueTemplate(VirtueTemplate virtueTemplate) {
		if (virtueTemplates == null) {
			virtueTemplates = new HashSet<VirtueTemplate>();
		}
		virtueTemplates.add(virtueTemplate);
	}

	public void removeVirtueTemplate(VirtueTemplate virtueTemplate) {
		virtueTemplates.remove(virtueTemplate);
	}

	public static VirtueUser anonymousUser() {
		return anonymousUser;
	}

	public static VirtueUser adminUser() {
		return adminUser;
	}

	@Override
	public String toString() {
		return "VirtueUser [username=" + username + ", authorities=" + authorities + ", virtueTemplates="
				+ virtueTemplates + ", virtueTemplateIds=" + virtueTemplateIds + ", enabled=" + enabled + "]";
	}

	public void removeAllVirtueTemplates() {
		virtueTemplates.clear();
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@JsonGetter
	public Collection<String> getVirtueTemplateIds() {
		if (virtueTemplates != null) {
			virtueTemplateIds = new ArrayList<String>();
			for (VirtueTemplate vt : virtueTemplates) {
				virtueTemplateIds.add(vt.getId());
			}
		}
		return virtueTemplateIds;
	}

	protected void setVirtueTemplateIds(Collection<String> virtueTemplateIds) {
		this.virtueTemplateIds = virtueTemplateIds;
	}

	public static boolean isAdmin(VirtueUser user) {
		return isRole(user, ROLE_ADMIN);
	}

	public static boolean isRole(VirtueUser user, String role) {
		if (user != null && user.getAuthorities() != null) {
			return user.getAuthorities().contains(role);
		}
		return false;
	}

	public static final Comparator<? super VirtueUser> USERNAME_COMPARATOR = new UsernameComparator();

	private static class UsernameComparator implements Comparator<VirtueUser> {
		@Override
		public int compare(VirtueUser o1, VirtueUser o2) {
			return String.CASE_INSENSITIVE_ORDER.compare(o1.getUsername(), o2.getUsername());
		}
	}
}
