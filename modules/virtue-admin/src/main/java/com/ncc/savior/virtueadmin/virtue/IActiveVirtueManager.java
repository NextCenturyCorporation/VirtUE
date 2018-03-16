package com.ncc.savior.virtueadmin.virtue;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.ncc.savior.virtueadmin.model.persistance.JpaVirtualMachine;
import com.ncc.savior.virtueadmin.model.persistance.JpaVirtueInstance;
import com.ncc.savior.virtueadmin.model.persistance.JpaVirtueTemplate;
import com.ncc.savior.virtueadmin.model.persistance.JpaVirtueUser;

import net.bytebuddy.agent.VirtualMachine;

/**
 * Manages and controls active {@link JpaVirtueInstance}s for the system. This
 * class is what converts {@link VirtueTemplate}s into
 * {@link JpaVirtueInstance}.
 * 
 *
 */
public interface IActiveVirtueManager {

	/**
	 * Returns all virtues that are assigned to the given user and were created from
	 * one of the given {@link VirtueTemplate} ids.
	 * 
	 * @param user
	 * @param keySet
	 * @return
	 */
	Map<String, Set<JpaVirtueInstance>> getVirtuesFromTemplateIds(JpaVirtueUser user, Set<String> keySet);

	/**
	 * Returns {@link VirtualMachine} that contains the given application in the
	 * given virtue.
	 * 
	 * @param virtueId
	 * @param applicationId
	 * @return
	 */
	JpaVirtualMachine getVmWithApplication(String virtueId, String applicationId);

	/**
	 * Initiates starting a virtual machine.
	 * 
	 * @param vm
	 * @return
	 */
	JpaVirtualMachine startVirtualMachine(JpaVirtualMachine vm);

	/**
	 * Handles the provisioning of all resources needed for a {@link VirtueTemplate}
	 * to create a usable {@link JpaVirtueInstance}.
	 * 
	 * @param user
	 * @param template
	 * @return
	 */
	JpaVirtueInstance provisionTemplate(JpaVirtueUser user, JpaVirtueTemplate template);

	void deleteVirtue(JpaVirtueUser user, String instanceId);

	Iterable<JpaVirtueInstance> getAllActiveVirtues();

	JpaVirtueInstance getActiveVirtue(String virtueId);

	Collection<JpaVirtueInstance> getVirtuesForUser(JpaVirtueUser user);

	JpaVirtueInstance getVirtueForUserFromTemplateId(JpaVirtueUser user, String instanceId);

	void adminDeleteVirtue(String instanceId);
}
