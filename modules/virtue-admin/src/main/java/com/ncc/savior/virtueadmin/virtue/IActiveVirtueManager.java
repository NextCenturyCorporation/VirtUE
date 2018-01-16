package com.ncc.savior.virtueadmin.virtue;

import java.util.Map;
import java.util.Set;

import com.ncc.savior.virtueadmin.model.User;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VirtueInstance;
import com.ncc.savior.virtueadmin.model.VirtueTemplate;

/**
 * Manages and controls active {@link VirtueInstance}s for the system. This
 * class is what converts {@link VirtueTemplate}s into {@link VirtueInstance}.
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
	Map<String, Set<VirtueInstance>> getVirtuesFromTemplateIds(User user, Set<String> keySet);

	/**
	 * Returns {@link VirtualMachine} that contains the given application in the
	 * given virtue.
	 * 
	 * @param virtueId
	 * @param applicationId
	 * @return
	 */
	VirtualMachine getVmWithApplication(String virtueId, String applicationId);

	/**
	 * Initiates starting a virtual machine.
	 * 
	 * @param vm
	 * @return
	 */
	VirtualMachine startVirtualMachine(VirtualMachine vm);

	/**
	 * Handles the provisioning of all resources needed for a {@link VirtueTemplate}
	 * to create a usable {@link VirtueInstance}.
	 * 
	 * @param user
	 * @param template
	 * @return
	 */
	VirtueInstance provisionTemplate(User user, VirtueTemplate template);

}
