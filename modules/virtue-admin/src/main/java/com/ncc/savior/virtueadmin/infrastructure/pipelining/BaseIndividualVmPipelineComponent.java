package com.ncc.savior.virtueadmin.infrastructure.pipelining;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.virtueadmin.model.persistance.JpaVirtualMachine;

import net.bytebuddy.agent.VirtualMachine;

/**
 * Base class for {@link IPipelineComponent} that handles much of the hard work.
 * This particular base class handles actions where the action itself can handle
 * only a single {@link VirtualMachine} at once. One example of this is SSH'ing
 * into a {@link VirtualMachine}. Since each {@link VirtualMachine} requires a
 * separate connection and has separate connection information, they cannot be
 * grouped.
 * 
 * This base class handles scheduling and handling success. Implementers much
 * implement {@link BaseIndividualVmPipelineComponent#onExecute(VirtualMachine)}
 * and call {@link #doOnSuccess(VirtualMachine)} when vms have succeeded.
 * {@link #doOnSuccess(VirtualMachine)} should only be called when the VM has
 * successfully been executed on. function.
 * 
 *
 */
public abstract class BaseIndividualVmPipelineComponent implements IPipelineComponent {
	private static final Logger logger = LoggerFactory.getLogger(BaseIndividualVmPipelineComponent.class);
	@SuppressWarnings("rawtypes")
	private Map<String, ScheduledFuture> futureMap;

	protected ScheduledExecutorService executor;
	private boolean isFixedRate = true;
	private long initialDelayMillis;
	private long periodOrDelayMillis;
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
	@SuppressWarnings("rawtypes")
	public BaseIndividualVmPipelineComponent(ScheduledExecutorService executor, boolean isFixedRate,
			long initialDelayMillis, long periodOrDelayMillis) {
		this.executor = executor;
		this.isFixedRate = isFixedRate;
		this.initialDelayMillis = initialDelayMillis;
		this.periodOrDelayMillis = periodOrDelayMillis;
		this.futureMap = Collections.synchronizedMap(new HashMap<String, ScheduledFuture>());
	}

	@Override
	public void addVirtualMachines(Collection<JpaVirtualMachine> vms) {
		logger.trace("Scheduling " + vms.size() + " vms to be renamed.");
		for (JpaVirtualMachine vm : vms) {
			Runnable command = getRunnable(vm);
			ScheduledFuture<?> future = schedule(command);
			futureMap.put(vm.getId(), future);
		}
	}

	/**
	 * handles when a VM failed in a way that they should not be retried and should
	 * be removed from the entire pipeline.
	 * 
	 * @param vm
	 */
	protected void doOnFailure(JpaVirtualMachine vm) {
		ScheduledFuture<?> future = futureMap.remove(vm.getId());
		if (future != null) {
			future.cancel(false);
		}
		resultListener.onFatalError(vm);
	}

	/**
	 * handles when a VM successfully complete the action for this
	 * {@link IPipelineComponent}. The VM will then be moved to the next
	 * {@link IPipelineComponent} in the pipeline.
	 * 
	 * @param vms
	 *            - all VMs that have successfully fulfilled the action for this
	 *            {@link IPipelineComponent}. These VM's will be saved and moved to
	 *            the next component in the pipeline.
	 */
	protected void doOnSuccess(JpaVirtualMachine vm) {
		ScheduledFuture<?> future = futureMap.remove(vm.getId());
		if (future != null) {
			future.cancel(false);
		}
		resultListener.onSuccess(vm, myIndexInPipeline);
	}

	protected Runnable getRunnable(JpaVirtualMachine vm) {
		Runnable command = new Runnable() {
			@Override
			public void run() {
				try {
				onExecute(vm);
				} catch (Throwable t) {
					logger.debug("Error in pipeline component runnable.  Component=" + this.getClass().getSimpleName(),
							t);
				}
			}
		};
		return command;
	}

	protected abstract void onExecute(JpaVirtualMachine vm);

	private ScheduledFuture<?> schedule(Runnable command) {
		ScheduledFuture<?> future = null;
		if (isFixedRate) {
			future = executor.scheduleAtFixedRate(command, initialDelayMillis, periodOrDelayMillis,
					TimeUnit.MILLISECONDS);
		} else {
			future = executor.scheduleWithFixedDelay(command, initialDelayMillis, periodOrDelayMillis,
					TimeUnit.MILLISECONDS);
		}
		return future;
	}

	@Override
	public void onPipelineStart(int index) {
		myIndexInPipeline = index;
	}

	@Override
	public void setResultListener(IUpdatePipelineResultListener resultListener) {
		this.resultListener = resultListener;
	}

}
