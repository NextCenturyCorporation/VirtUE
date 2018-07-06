package com.ncc.savior.virtueadmin.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Class to represent a user that has been authenticated by the security
 * service. What we want in here is TDB.
 * 
 *
 */
@Entity
public class VirtueUser {
	private static final VirtueUser testUser;
	private static final VirtueUser anonymousUser;
	private static VirtueUser adminUser;

	@Id
	private String username;
	@ElementCollection(targetClass = String.class)
	private Collection<String> authorities;
	@ManyToMany()
	private Collection<VirtueTemplate> virtueTemplates;
	@Transient
	private Collection<String> virtueTemplateIds;
	private boolean enabled;

	static {
		testUser = new VirtueUser("testUser", new ArrayList<String>(), true);
		anonymousUser = new VirtueUser("anonymous", new ArrayList<String>(), true);
		ArrayList<String> adminAuths = new ArrayList<String>();
		adminAuths.add("ROLE_ADMIN");
		adminAuths.add("ROLE_USER");
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

}
