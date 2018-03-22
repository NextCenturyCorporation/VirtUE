package com.ncc.savior.virtueadmin.infrastructure.pipelining;

import java.util.Collection;

import com.ncc.savior.virtueadmin.model.VirtualMachine;

/**
 * Listener to handle results for VM's in an {@link IUpdatePipeline}.
 * 
 *
 */
public interface IUpdatePipelineResultListener {

	/**
	 * Called on success
	 * 
	 * @param vm
	 * @param currentPipelineIndex
	 *            - the index of the current pipeline so the {@link IUpdatePipeline}
	 *            knows how to handle the {@link VirtualMachine}.
	 */
	void onSuccess(VirtualMachine vm, int currentPipelineIndex);

	/**
	 * Called on success
	 * 
	 * @param vms
	 * @param currentPipelineIndex
	 *            - the index of the current pipeline so the {@link IUpdatePipeline}
	 *            knows how to handle the {@link VirtualMachine}.
	 */
	void onSuccess(Collection<VirtualMachine> vms, int currentPipelineIndex);

	/**
	 * Should be removed from Pipeline
	 * 
	 * @param vm
	 * @param currentPipelineIndex
	 */
	void onFatalError(VirtualMachine vm);

	/**
	 * Should be removed from Pipeline
	 * 
	 * @param vm
	 * @param currentPipelineIndex
	 */
	void onFatalError(Collection<VirtualMachine> vms);

}
