package com.ncc.savior.virtueadmin.virtue;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VirtueInstance;
import com.ncc.savior.virtueadmin.model.VirtueTemplate;
import com.ncc.savior.virtueadmin.model.VirtueUser;

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
	Map<String, Set<VirtueInstance>> getVirtuesFromTemplateIds(VirtueUser user, Set<String> keySet);

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
	VirtueInstance provisionTemplate(VirtueUser user, VirtueTemplate template);

	VirtueInstance deleteVirtue(VirtueUser user, String instanceId);

	Iterable<VirtueInstance> getAllActiveVirtues();

	VirtueInstance getActiveVirtue(String virtueId);

	Collection<VirtueInstance> getVirtuesForUser(VirtueUser user);

	VirtueInstance getVirtueForUserFromTemplateId(VirtueUser user, String instanceId);

	void adminDeleteVirtue(String instanceId);

	VirtueInstance startVirtue(VirtueUser user, String virtueId);

	VirtueInstance stopVirtue(VirtueUser user, String virtueId);

	VirtualMachine getVm(String id);

	Iterable<VirtualMachine> getAllVirtualMachines();

	void rebootVm(String vmId);

	void sync();

}
