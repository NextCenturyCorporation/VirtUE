package com.ncc.savior.virtueadmin.model.persistance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;

import com.ncc.savior.virtueadmin.model.BaseVirtueUser;

/**
 * Class to represent a user that has been authenticated by the security
 * service. What we want in here is TDB.
 * 
 *
 */
@Entity
public class JpaVirtueUser extends BaseVirtueUser {

	@ManyToMany()
	private Collection<JpaVirtueTemplate> virtueTemplates;

	protected JpaVirtueUser() {

	}

	public JpaVirtueUser(String name, Collection<String> authorities) {
		super(name, authorities);
		this.virtueTemplates = new HashSet<JpaVirtueTemplate>();
	}



	public Collection<JpaVirtueTemplate> getVirtueTemplates() {
		return virtueTemplates;
	}

	public void addVirtueTemplate(JpaVirtueTemplate virtueTemplate) {
		virtueTemplates.add(virtueTemplate);
	}

	public void removeVirtueTemplate(JpaVirtueTemplate virtueTemplate) {
		virtueTemplates.remove(virtueTemplate);
	}


	@Override
	public String toString() {
		return "JpaVirtueUser [virtueTemplates=" + virtueTemplates + ", username=" + username + ", authorities="
				+ authorities + "]";
	}

	public void removeAllVirtueTemplates() {
		virtueTemplates.clear();
	}

	public static JpaVirtueUser anonymousUser() {
		return new JpaVirtueUser("anonymous", new ArrayList<String>());
	}

}
