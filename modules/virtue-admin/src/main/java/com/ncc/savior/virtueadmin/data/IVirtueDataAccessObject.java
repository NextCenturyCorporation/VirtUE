package com.ncc.savior.virtueadmin.data;

import java.util.List;

import com.ncc.savior.virtueadmin.model.Application;
import com.ncc.savior.virtueadmin.model.User;
import com.ncc.savior.virtueadmin.model.VirtueInstance;
import com.ncc.savior.virtueadmin.model.VirtueTemplate;

/**
 * Interface for virtues backend data store.
 * 
 *
 */
public interface IVirtueDataAccessObject {

	List<VirtueTemplate> getTemplatesForUser(User user);

	List<VirtueInstance> getVirtuesForUser(User user);

	VirtueTemplate getTemplate(String templateId);

	VirtueInstance getVirtue(String virtueId);

	List<Application> getApplicationsForVirtue(User user, String virtueId);

	void addVirtueForUser(User user, VirtueInstance virtue);

}
