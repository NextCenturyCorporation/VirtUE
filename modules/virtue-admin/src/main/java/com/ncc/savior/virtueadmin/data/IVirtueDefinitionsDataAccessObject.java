package com.ncc.savior.virtueadmin.data;

import java.util.List;

import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.User;
import com.ncc.savior.virtueadmin.model.VirtueInstance;
import com.ncc.savior.virtueadmin.model.VirtueState;
import com.ncc.savior.virtueadmin.model.VirtueTemplate;
import com.ncc.savior.virtueadmin.model.VmState;
import com.ncc.savior.virtueadmin.model.desktop.DesktopVirtue;

/**
 * Interface for virtues backend data store.
 * 
 *
 */
public interface IVirtueDefinitionsDataAccessObject {

	List<VirtueTemplate> getTemplatesForUser(User user);

	List<VirtueInstance> getVirtuesForUser(User user);

	VirtueTemplate getTemplate(String templateId);

	VirtueInstance getVirtue(String virtueId);

	List<ApplicationDefinition> getApplicationsForVirtue(User user, String virtueId);

	void addVirtueForUser(User user, VirtueInstance virtue);

	void updateVirtueState(String virtueId, VirtueState state);

	void updateVmState(String virtueId, String vmId, VmState state);

	List<DesktopVirtue> getVirtueListForUser(User user);
}
