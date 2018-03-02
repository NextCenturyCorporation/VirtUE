package com.ncc.savior.virtueadmin.data;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.ncc.savior.virtueadmin.model.VirtueUser;
import com.ncc.savior.virtueadmin.model.AbstractVirtualMachine;
import com.ncc.savior.virtueadmin.model.VirtueInstance;
import com.ncc.savior.virtueadmin.model.VmState;

/**
 * Data access object for Active Virtues. This class manages the storage and
 * retrieval of Virtue information from a data storage system.
 * 
 *
 */
public interface IActiveVirtueDao {

	/**
	 * Returns all virtues that are assigned to the given user and are build from
	 * one of the given templateIds.
	 * 
	 * @param user
	 * 
	 * @param templateIds
	 * @return
	 */
	Map<String, Set<VirtueInstance>> getVirtuesFromTemplateIds(VirtueUser user, Set<String> templateIds);

	/**
	 * Updates the state of a given VM.
	 * 
	 * @param vmId
	 * @param state
	 */
	void updateVmState(String vmId, VmState state);

	/**
	 * Return the given Virtual Machine in a virtue that has the given application.
	 * 
	 * @param virtueId
	 * @param applicationId
	 * @return
	 */
	AbstractVirtualMachine getVmWithApplication(String virtueId, String applicationId);

	/**
	 * Adds a new virtue to the data store.
	 * 
	 * @param vi
	 */
	void addVirtue(VirtueInstance vi);

	Optional<VirtueInstance> getVirtueInstance(String virtueId);

	Iterable<VirtueInstance> getAllActiveVirtues();

	void clear();

}
