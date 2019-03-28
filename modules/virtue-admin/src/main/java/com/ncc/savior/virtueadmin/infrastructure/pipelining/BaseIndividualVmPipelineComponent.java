/*
 * Copyright (C) 2019 Next Century Corporation
 * 
 * This file may be redistributed and/or modified under either the GPL
 * 2.0 or 3-Clause BSD license. In addition, the U.S. Government is
 * granted government purpose rights. For details, see the COPYRIGHT.TXT
 * file at the root of this project.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 * 
 * SPDX-License-Identifier: (GPL-2.0-only OR BSD-3-Clause)
 */
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

import com.ncc.savior.util.SaviorException;
import com.ncc.savior.virtueadmin.model.VirtualMachine;

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
public abstract class BaseIndividualVmPipelineComponent<T> implements IPipelineComponent<T> {
	private static final Logger logger = LoggerFactory.getLogger(BaseIndividualVmPipelineComponent.class);
	@SuppressWarnings("rawtypes")
	private Map<String, ScheduledFuture> futureMap;

	protected ScheduledExecutorService executor;
	private boolean isFixedRate = true;
	private long initialDelayMillis;
	private long periodOrDelayMillis;
	private IUpdatePipelineResultListener<T> resultListener;
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
	public void addPipelineElements(Collection<PipelineWrapper<T>> wrappers) {
		logger.trace("Scheduling " + wrappers.size() + " vms to be renamed.");
		for (PipelineWrapper<T> wrapper : wrappers) {
			Runnable command = getRunnable(wrapper);
			ScheduledFuture<?> future = schedule(command);
			futureMap.put(getId(wrapper.get()), future);
		}
	}

	/**
	 * handles when a VM failed in a way that they should not be retried and should
	 * be removed from the entire pipeline.
	 * 
	 * @param element
	 */
	protected void doOnFailure(PipelineWrapper<T> wrapper) {
		ScheduledFuture<?> future = futureMap.remove(getId(wrapper.get()));
		if (future != null) {
			future.cancel(false);
		}
		resultListener.onFatalError(wrapper);
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
	protected void doOnSuccess(PipelineWrapper<T> wrapper) {
		T element = wrapper.get();
		ScheduledFuture<?> future = futureMap.remove(getId(element));
		if (future != null) {
			future.cancel(false);
		}
		resultListener.onSuccess(wrapper, myIndexInPipeline);
	}

	protected Runnable getRunnable(PipelineWrapper<T> pipelineWrapper) {
		Runnable command = new Runnable() {
			@Override
			public void run() {
				try {
					onExecute(pipelineWrapper);
				} catch (SaviorException e) {
					doOnFailure(pipelineWrapper);
				} catch (Throwable t) {
					logger.debug("Error in pipeline component runnable.  Component=" + this.getClass().getSimpleName(),
							t);
				}
			}
		};
		return command;
	}

	protected abstract void onExecute(PipelineWrapper<T> pipelineWrapper);

	protected abstract String getId(T element);

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
	public void setResultListener(IUpdatePipelineResultListener<T> resultListener) {
		this.resultListener = resultListener;
	}

}
