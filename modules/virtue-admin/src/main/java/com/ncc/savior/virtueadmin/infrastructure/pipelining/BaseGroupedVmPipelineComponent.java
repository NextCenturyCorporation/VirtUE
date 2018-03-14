package com.ncc.savior.virtueadmin.infrastructure.pipelining;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.ncc.savior.virtueadmin.model.VirtualMachine;

/**
 * Base class for {@link IPipelineComponent} that handles much of the hard work.
 * This particular base class handles actions where the action itself can handle
 * many {@link VirtualMachine}s at once. One example of this is getting the
 * networking information from AWS. Since AWS's API allows sending multiple VM
 * instance IDs in a single request, we can send them all at once.
 * 
 * This base class handles scheduling and handling success. Implementers much
 * implement {@link BaseGroupedVmPipelineComponent#onExecute(ArrayList)} and
 * call {@link #doOnSuccess(Collection)} when vms have succeeded. Only
 * successful VM's should be passed to the {@link #doOnSuccess(Collection)}
 * function.
 * 
 *
 */
public abstract class BaseGroupedVmPipelineComponent implements IPipelineComponent {

	private ScheduledExecutorService executor;
	private boolean isFixedRate;
	private long initialDelayMillis;
	private long periodOrDelayMillis;
	ScheduledFuture<?> future = null;
	protected Collection<VirtualMachine> vmCollection;
	private IUpdatePipelineResultListener resultListener;
	private int myIndexInPipeline;

	/**
	 * 
	 * @param executor
	 * @param isFixedRate
	 *            - should the action be executed at a fixed rate/period (every X
	 *            seconds no matter how long the action takes) or at a certain
	 *            interval (every X seconds after the action completes). True means
	 *            the former.
	 * @param initialDelayMillis
	 *            - Delay in milliseconds from scheduling to when the action should
	 *            take place.
	 * @param periodOrDelayMillis
	 *            - period or interval delay in milliseconds between execution
	 *            calls.
	 */
	public BaseGroupedVmPipelineComponent(ScheduledExecutorService executor, boolean isFixedRate,
			long initialDelayMillis, long periodOrDelayMillis) {
		this.executor = executor;
		this.isFixedRate = isFixedRate;
		this.initialDelayMillis = initialDelayMillis;
		this.periodOrDelayMillis = periodOrDelayMillis;
		this.vmCollection = Collections.synchronizedCollection(new ArrayList<VirtualMachine>());
	}

	@Override
	public void addVirtualMachines(Collection<VirtualMachine> vms) {
		vmCollection.addAll(vms);
	}

	@Override
	public void onPipelineStart(int index) {
		myIndexInPipeline = index;
		Runnable command = new Runnable() {
			@Override
			public void run() {
				ArrayList<VirtualMachine> vms = new ArrayList<VirtualMachine>(vmCollection);
				onExecute(vms);
			}
		};
		if (isFixedRate) {
			this.future = executor.scheduleAtFixedRate(command, initialDelayMillis, periodOrDelayMillis,
					TimeUnit.MILLISECONDS);
		} else {
			this.future = executor.scheduleWithFixedDelay(command, initialDelayMillis, periodOrDelayMillis,
					TimeUnit.MILLISECONDS);
		}
	}

	/**
	 * handles when VMs successfully complete the action for this
	 * {@link IPipelineComponent}. The VM will then be moved to the next
	 * {@link IPipelineComponent} in the pipeline.
	 * 
	 * @param vms
	 *            - all VMs that have successfully fulfilled the action for this
	 *            {@link IPipelineComponent}. These VM's will be saved and moved to
	 *            the next component in the pipeline.
	 */
	protected void doOnSuccess(Collection<VirtualMachine> vms) {
		vmCollection.removeAll(vms);
		resultListener.onSuccess(vms, myIndexInPipeline);
	}

	/**
	 * handles when VMs failed in a way that they should not be retried and should
	 * be removed from the entire pipeline.
	 * 
	 * @param vms
	 */
	protected void doOnFailure(Collection<VirtualMachine> vms) {
		vmCollection.removeAll(vms);
		resultListener.onFatalError(vms);
	}

	/**
	 * Implementers need to implement this function with the particular action to be
	 * performed on the given {@link VirtualMachine}s. This function MUST also call
	 * {@link #doOnSuccess(Collection)} when the {@link VirtualMachine}s have the
	 * action successfully performed on them. The {@link VirtualMachine}s passed to
	 * {@link #doOnSuccess(Collection)} must be only the VM's that succeeded if some
	 * succeeded and some failed.
	 * 
	 * Implementers can also call {@link #doOnFailure(Collection)} to remove VM's
	 * from the pipeline
	 * 
	 * @param vms
	 */
	protected abstract void onExecute(ArrayList<VirtualMachine> vms);

	@Override
	public void setResultListener(IUpdatePipelineResultListener resultListener) {
		this.resultListener = resultListener;
	}

}
