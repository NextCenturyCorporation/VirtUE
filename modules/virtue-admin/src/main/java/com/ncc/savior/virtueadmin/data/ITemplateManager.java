package com.ncc.savior.virtueadmin.data;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.VirtueUser;
import com.ncc.savior.virtueadmin.model.VirtualMachineTemplate;
import com.ncc.savior.virtueadmin.model.VirtueTemplate;

/**
 * Manages and stores all template information and definitions.
 * 
 *
 */
public interface ITemplateManager {
	/**
	 * Returns a template for the given id if the given user has been assigned that
	 * virtue template.
	 * 
	 * @param user
	 * @param templateId
	 * @return
	 */
	VirtueTemplate getVirtueTemplateForUser(VirtueUser user, String templateId);

	/**
	 * Returns all the {@link VirtueTemplate} that the given user has access to.
	 * 
	 * @param user
	 * @return
	 */
	Map<String, VirtueTemplate> getVirtueTemplatesForUser(VirtueUser user);

	/**
	 * Returns all the {@link VirtueTemplate} ids that the given user has access to.
	 * 
	 * @param user
	 * @return
	 */
	Collection<String> getVirtueTemplateIdsForUser(VirtueUser user);

	void assignApplicationToVmTemplate(String vmTemplateId, String applicationId);

	void assingVmTemplateToVirtueTemplate(String virtueTemplate, String vmTemplateId);

	/**
	 * Assigns a virtue to a user such that the user now has the ability to use that
	 * {@link VirtueTemplate} from the id.
	 * 
	 * @param user
	 * @param virtueTemplateId
	 */
	void assignVirtueTemplateToUser(VirtueUser user, String virtueTemplateId);

	/**
	 * Removes the given virtue from the list of virtues that the user has the
	 * ability to use. Calling this function does not affect any existing virtues,
	 * but only the data store.
	 * 
	 * @param user
	 * @param virtueTemplateId
	 */
	void revokeVirtueTemplateFromUser(VirtueUser user, String virtueTemplateId);

	/**
	 * Returns all {@link VirtueTemplate} in the data store.
	 * 
	 * @return
	 */
	Iterable<VirtueTemplate> getAllVirtueTemplates();

	/**
	 * Returns all {@link VirtualMachineTemplate} in the data store.
	 * 
	 * @return
	 */
	
	Iterable<VirtualMachineTemplate> getAllVirtualMachineTemplates();

	Iterable<ApplicationDefinition> getAllApplications();

	/**
	 * Returns all the {@link ApplicationDefinition}s in the data store.
	 * 
	 * @param applicationId
	 * @return
	 */
	Optional<ApplicationDefinition> getApplicationDefinition(String applicationId);
//	ApplicationDefinition getApplicationDefinition(String applicationId);

	Optional<VirtueTemplate> getVirtueTemplate(String templateId);

	Optional<VirtualMachineTemplate> getVmTemplate(String templateId);

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

	Collection<String> getUsersWithTemplate();

	void clear();

	void deleteApplicationDefinition(String templateId);

	void deleteVmTemplate(String templateId);

	void deleteVirtueTemplate(String templateId);
}
