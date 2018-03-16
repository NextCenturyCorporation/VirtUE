package com.ncc.savior.virtueadmin.data;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.persistance.JpaVirtualMachineTemplate;
import com.ncc.savior.virtueadmin.model.persistance.JpaVirtueTemplate;
import com.ncc.savior.virtueadmin.model.persistance.JpaVirtueUser;

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
	JpaVirtueTemplate getVirtueTemplateForUser(JpaVirtueUser user, String templateId);

	/**
	 * Returns all the {@link VirtueTemplate} that the given user has access to.
	 * 
	 * @param user
	 * @return
	 */
	Map<String, JpaVirtueTemplate> getVirtueTemplatesForUser(JpaVirtueUser user);

	/**
	 * Returns all the {@link VirtueTemplate} ids that the given user has access to.
	 * 
	 * @param user
	 * @return
	 */
	Collection<String> getVirtueTemplateIdsForUser(JpaVirtueUser user);

	void assignApplicationToVmTemplate(String vmTemplateId, String applicationId);

	void assingVmTemplateToVirtueTemplate(String virtueTemplate, String vmTemplateId);

	/**
	 * Assigns a virtue to a user such that the user now has the ability to use that
	 * {@link VirtueTemplate} from the id.
	 * 
	 * @param user
	 * @param virtueTemplateId
	 */
	void assignVirtueTemplateToUser(JpaVirtueUser user, String virtueTemplateId);

	/**
	 * Removes the given virtue from the list of virtues that the user has the
	 * ability to use. Calling this function does not affect any existing virtues,
	 * but only the data store.
	 * 
	 * @param user
	 * @param virtueTemplateId
	 */
	void revokeVirtueTemplateFromUser(JpaVirtueUser user, String virtueTemplateId);

	/**
	 * Returns all {@link VirtueTemplate} in the data store.
	 * 
	 * @return
	 */
	Iterable<JpaVirtueTemplate> getAllVirtueTemplates();

	/**
	 * Returns all {@link VirtualMachineTemplate} in the data store.
	 * 
	 * @return
	 */
	
	Iterable<JpaVirtualMachineTemplate> getAllVirtualMachineTemplates();

	Iterable<ApplicationDefinition> getAllApplications();

	/**
	 * Returns all the {@link ApplicationDefinition}s in the data store.
	 * 
	 * @param applicationId
	 * @return
	 */
	Optional<ApplicationDefinition> getApplicationDefinition(String applicationId);
//	ApplicationDefinition getApplicationDefinition(String applicationId);

	Optional<JpaVirtueTemplate> getVirtueTemplate(String templateId);

	Optional<JpaVirtualMachineTemplate> getVmTemplate(String templateId);

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
	void addVmTemplate(JpaVirtualMachineTemplate vmTemplate);

	/**
	 * adds a new {@link VirtueTemplate} to the data store.
	 * 
	 * @param template
	 */
	void addVirtueTemplate(JpaVirtueTemplate template);

	Collection<String> getUsersWithTemplate();

	void clear();

	void deleteApplicationDefinition(String templateId);

	void deleteVmTemplate(String templateId);

	void deleteVirtueTemplate(String templateId);

	void test();

	Iterable<JpaVirtualMachineTemplate> getVmTemplatesById(Collection<String> vmTemplateIds);

	Iterable<ApplicationDefinition> getApplicationDefinitions(Collection<String> applicationIds);
}
