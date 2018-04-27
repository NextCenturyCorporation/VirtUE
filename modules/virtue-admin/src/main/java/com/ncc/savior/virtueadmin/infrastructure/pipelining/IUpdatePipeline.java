package com.ncc.savior.virtueadmin.infrastructure.pipelining;

import java.util.Collection;

import com.ncc.savior.virtueadmin.model.VirtualMachine;

/**
 * An {@link IUpdatePipeline} is useful for performing tasks on VM's where there
 * is a series of tasks that may or may not success on first try, but should be
 * executed in order. The VM will be passed through {@link IPipelineComponent}s
 * as it succeeds on whatever action the pipeline component is expected to
 * perform.
 * 
 * Component instances cannot be reused between different pipelines!
 * 
 *
 */
public interface IUpdatePipeline<T> {
	/**
	 * Add {@link IPipelineComponent} to a specific index. If index is out of
	 * bounds, the component should be added to the specific end. I.E. negative
	 * numbers should be added at the beginning. High numbers should be added to the
	 * end.
	 * 
	 * Component instances cannot be reused between different pipelines!
	 * 
	 * @param component
	 * @param index
	 */
	void addPipelineComponent(IPipelineComponent<T> component, int index);

	/**
	 * Add {@link IPipelineComponent} to the end of the {@link IUpdatePipeline}.
	 * 
	 * Component instances cannot be reused between different pipelines!
	 * 
	 * @param component
	 */
	void addPipelineComponent(IPipelineComponent<T> component);

	/**
	 * Add {@link VirtualMachine}s to the {@link IUpdatePipeline} to be processed.
	 * Adding {@link VirtualMachine}s to the pipeline before starting is not
	 * supported and the results will be implementation specific.
	 * 
	 * @param vms
	 */
	void addToPipeline(Collection<T> vms);

	/**
	 * Starts the {@link IUpdatePipeline}. Typically, this just tells each component
	 * that the pipeline has started. Adding {@link VirtualMachine}s to the pipeline
	 * before it is started is not supported.
	 */
	void start();

}
