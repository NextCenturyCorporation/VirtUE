package com.ncc.savior.virtueadmin.infrastructure.pipelining;

import java.util.Collection;

import com.ncc.savior.virtueadmin.model.VirtualMachine;

/**
 * A component of an {@link IUpdatePipeline}. Each pipeline does an action on
 * VMs and retries until it succeeds.
 * 
 *
 */
public interface IPipelineComponent {

	/**
	 * Adds {@link VirtualMachine}s to this particular {@link IPipelineComponent}.
	 * 
	 * @param vms
	 */
	void addVirtualMachines(Collection<VirtualMachine> vms);

	/**
	 * Called when an {@link IUpdatePipeline} is started so
	 * {@link IPipelineComponent}s can perform initial startup actions and store
	 * their index. The index is used later in callbacks.
	 * 
	 * @param index
	 */
	void onPipelineStart(int index);

	/**
	 * Store a listener to indicate when VM's have succeeded or failed and should
	 * either progress through the pipeline or be removed from the pipeline.
	 * 
	 * @param updatePipeline
	 */
	void setResultListener(IUpdatePipelineResultListener updatePipeline);

}
