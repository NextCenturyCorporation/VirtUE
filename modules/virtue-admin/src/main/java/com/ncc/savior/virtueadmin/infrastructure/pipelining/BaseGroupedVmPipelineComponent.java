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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public abstract class BaseGroupedVmPipelineComponent<T> implements IPipelineComponent<T> {
	private static final Logger logger = LoggerFactory.getLogger(BaseGroupedVmPipelineComponent.class);
	private ScheduledExecutorService executor;
	private boolean isFixedRate;
	private long initialDelayMillis;
	private long periodOrDelayMillis;
	ScheduledFuture<?> future = null;
	protected Collection<PipelineWrapper<T>> collection;
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
	public BaseGroupedVmPipelineComponent(ScheduledExecutorService executor, boolean isFixedRate,
			long initialDelayMillis, long periodOrDelayMillis) {
		this.executor = executor;
		this.isFixedRate = isFixedRate;
		this.initialDelayMillis = initialDelayMillis;
		this.periodOrDelayMillis = periodOrDelayMillis;
		this.collection = Collections.synchronizedCollection(new ArrayList<PipelineWrapper<T>>());
	}

	@Override
	public void addPipelineElements(Collection<PipelineWrapper<T>> wrappers) {
		collection.addAll(wrappers);
	}

	@Override
	public void onPipelineStart(int index) {
		myIndexInPipeline = index;
		Runnable command = new Runnable() {
			@Override
			public void run() {
				try {
					if (!collection.isEmpty()) {
						ArrayList<PipelineWrapper<T>> elements = new ArrayList<PipelineWrapper<T>>(collection);
						onExecute(elements);
					}
				} catch (Throwable t) {
					logger.debug("Error in pipeline component runnable.  Component=" + this.getClass().getSimpleName(),
							t);
				}
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
	protected void doOnSuccess(Collection<PipelineWrapper<T>> vms) {
		collection.removeAll(vms);
		resultListener.onSuccess(vms, myIndexInPipeline);
	}

	/**
	 * handles when VMs failed in a way that they should not be retried and should
	 * be removed from the entire pipeline.
	 * 
	 * @param vms
	 */
	protected void doOnFailure(Collection<PipelineWrapper<T>> vms) {
		collection.removeAll(vms);
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
	protected abstract void onExecute(Collection<PipelineWrapper<T>> vms);

	@Override
	public void setResultListener(IUpdatePipelineResultListener<T> resultListener) {
		this.resultListener = resultListener;
	}

	protected Collection<T> unwrap(Collection<PipelineWrapper<T>> wrapper) {
		Collection<T> col = new ArrayList<T>(wrapper.size());
		for (PipelineWrapper<T> p : wrapper) {
			col.add(p.get());
		}
		return col;
	}

}
