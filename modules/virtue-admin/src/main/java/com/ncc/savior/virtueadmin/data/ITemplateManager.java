package com.ncc.savior.virtueadmin.data;

import java.util.Collection;
import java.util.Map;

import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.User;
import com.ncc.savior.virtueadmin.model.VirtualMachineTemplate;
import com.ncc.savior.virtueadmin.model.VirtueTemplate;

/**
 * Manages and stores all template information and definitions.
 * 
 *
 */
public interface ITemplateManager {
	/**
	 * Returns all {@link VirtueTemplate} in the data store.
	 * 
	 * @return
	 */
	Collection<VirtueTemplate> getAllVirtueTemplates();

	/**
	 * Returns all {@link VirtualMachineTemplate} in the data store.
	 * 
	 * @return
	 */
	Collection<VirtualMachineTemplate> getAllVirtualMachineTemplates();

	/**
	 * Returns all the {@link VirtueTemplate} that the given user has access to.
	 * 
	 * @param user
	 * @return
	 */
	Map<String, VirtueTemplate> getVirtueTemplatesForUser(User user);

	/**
	 * Returns all the {@link VirtueTemplate} ids that the given user has access to.
	 * 
	 * @param user
	 * @return
	 */
	Collection<String> getVirtueTemplateIdsForUser(User user);

	/**
	 * Returns all the {@link ApplicationDefinition}s in the data store.
	 * 
	 * @param applicationId
	 * @return
	 */
	ApplicationDefinition getApplicationDefinition(String applicationId);

	/**
	 * Returns a template for the given id if the given user has been assigned that
	 * virtue template.
	 * 
	 * @param user
	 * @param templateId
	 * @return
	 */
	VirtueTemplate getTemplate(User user, String templateId);

	/**
	 * Adds a new {@link ApplicationDefinition} to the data store.
	 * 
	 * @param app
	 */
	void addApplicationDefinition(ApplicationDefinition app);

	/**
	 * Adds a new {@link VirtualMachineTemplate} to the data store.
	 * 
	 * @param vmTemplate
	 */
	void addVmTemplate(VirtualMachineTemplate vmTemplate);

	/**
	 * adds a new {@link VirtueTemplate} to the data store.
	 * 
	 * @param template
	 */
	void addVirtueTemplate(VirtueTemplate template);

	/**
	 * Assigns a virtue to a user such that the user now has the ability to use that
	 * {@link VirtueTemplate} from the id.
	 * 
	 * @param user
	 * @param virtueTemplateId
	 */
	void assignVirtueTemplateToUser(User user, String virtueTemplateId);

	/**
	 * Removes the given virtue from the list of virtues that the user has the
	 * ability to use. Calling this function does not affect any existing virtues,
	 * but only the data store.
	 * 
	 * @param user
	 * @param virtueTemplateId
	 */
	void revokeVirtueTemplateFromUser(User user, String virtueTemplateId);
}
