package com.ncc.savior.virtueadmin.infrastructure;

import java.util.Collection;

import com.ncc.savior.virtueadmin.model.VmState;
import com.ncc.savior.virtueadmin.model.persistance.JpaVirtualMachine;
import com.ncc.savior.virtueadmin.model.persistance.JpaVirtualMachineTemplate;
import com.ncc.savior.virtueadmin.model.persistance.JpaVirtueUser;

import net.bytebuddy.agent.VirtualMachine;

/**
 * Manages the low level instantiations of Virtual Machines, their control and
 * provisioning.
 * 
 *
 */
public interface IVmManager {
	/**
	 * Adds a listener which will be notified when the state of a VM has been
	 * updated. It is not guaranteed that all state updates will be notified, but
	 * Asynchronous actions should be notified (I.E. Starting to Running, Stopping
	 * to Stopped, etc).
	 * 
	 * @param listener
	 */
	public void addVmUpdateListener(IVmUpdateListener listener);

	/**
	 * Removes a listener which would have been notified when the state of a VM has
	 * been updated.
	 * 
	 * @param listener
	 */
	public void removeVmUpdateListener(IVmUpdateListener listener);

	/**
	 * Provide a provisioned VM that is currently not used by another Virtue that
	 * was generated from the given template. Once provisioned, the VM should be
	 * ready to be started. The exact method that this provision occurs is
	 * implementation specific. For example, an implementation could have a pool vms
	 * implementing all available templates and just take one from that pool.
	 * Alternatively, the obvious implementation is creating a brand new VM from the
	 * given template.
	 * 
	 * @param vmt
	 * @return
	 */
	JpaVirtualMachine provisionVirtualMachineTemplate(JpaVirtueUser user, JpaVirtualMachineTemplate vmt);

	/**
	 * Initiates a start action on the provided VM. It is not guaranteed that the VM
	 * will be started when this function returns. However, if the VM is not started
	 * upon return, the implementation should notify the
	 * {@link IStateUpdateListener}s when it has finished starting.
	 * 
	 * @param vm
	 * @return
	 */
	public JpaVirtualMachine startVirtualMachine(JpaVirtualMachine vm);

	public Collection<JpaVirtualMachine> startVirtualMachines(Collection<JpaVirtualMachine> vms);

	/**
	 * Initiates a stop action on the provided VM. It is not guaranteed that the VM
	 * will be stopped when this function returns. However, if the VM is not stopped
	 * upon return, the implementation should notify the
	 * {@link IStateUpdateListener}s when it has finished stopping.
	 * 
	 * @param vm
	 * @return
	 */
	public JpaVirtualMachine stopVirtualMachine(JpaVirtualMachine vm);

	public Collection<JpaVirtualMachine> stopVirtualMachines(Collection<JpaVirtualMachine> vms);

	/**
	 * Calling this function notifies that the VM is no longer needed and the
	 * manager is free to reclaim its resources. The obvious implementation should
	 * just delete the VM, however, the manager could save it and repurpose it if
	 * necessary.
	 * 
	 * @param vm
	 */
	// TODO TBD if a VM is repurposed, who is responsible for reseting user access
	// and user data?
	public void deleteVirtualMachine(JpaVirtualMachine vm);

	/**
	 * Deletes all vms
	 * 
	 * @param vms
	 */
	public void deleteVirtualMachines(Collection<JpaVirtualMachine> vms);

	/**
	 * Returns the current state of the given Virtual Machine.
	 * 
	 * @param vm
	 * @return
	 */
	public VmState getVirtualMachineState(JpaVirtualMachine vm);

	/**
	 * Convenience/Performance function to provision multiple VMs at one time. See
	 * details from {@link IVmManager#startVirtualMachine(VirtualMachine)}.
	 * 
	 * @param user
	 * 
	 * @param vmTemplates
	 * @return
	 */
	public Collection<JpaVirtualMachine> provisionVirtualMachineTemplates(JpaVirtueUser user,
			Collection<JpaVirtualMachineTemplate> vmTemplates);

}
