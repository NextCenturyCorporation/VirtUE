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
package com.ncc.savior.virtueadmin.infrastructure.future;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.elasticmapreduce.util.ResizeJobFlowStep.OnFailure;
import com.ncc.savior.util.SaviorException;

/**
 * Implementation of {@link BaseCompletableFutureService} where multiple
 * parameters P can be executed at the same time to save on efficiency. In this
 * variant, this class manages a single {@link ScheduledFuture} which will call
 * {@link #onExecute(Collection)} of the concrete implementation and pass all of
 * the P's along with their future and extra value in a {@link Wrapper} class.
 * The collection passed is a copy and can be manipulated.
 * 
 * The concrete implementation is expected to implement
 * {@link #onExecute(Collection)} and call either {@link #onSuccess(Collection)}
 * or one of the
 * {@link #onFailure(com.ncc.savior.virtueadmin.infrastructure.future.BaseCompletableFutureService.Wrapper)},
 * {@link OnFailure} methods provided here. They should not call any onFailure
 * methods provided by the super class {@link BaseCompletableFutureService}
 * 
 *
 * @param <P>
 *            - input parameter to service that will be given from
 *            {@link CompletableFuture} (usually from previous service)
 * @param <R>
 *            - Return type parameter that is returned via return
 *            {@link CompletableFuture} (often goes to the next service)
 * @param <X>
 *            - extra information class. Can be any class and is just passed
 *            along with the data. This could be a virtue id, for example.
 */

public abstract class BaseGroupedScheduledCompletableFutureService<P, R, X>
		extends BaseCompletableFutureService<P, R, X> {

	private static final Logger logger = LoggerFactory.getLogger(BaseGroupedScheduledCompletableFutureService.class);

	private ScheduledExecutorService executor;
	private boolean isFixedRate;
	private long initialDelayMillis;
	private long periodOrDelayMillis;
	private Collection<Wrapper> collection;

	protected BaseGroupedScheduledCompletableFutureService(ScheduledExecutorService executor, boolean isFixedRate,
			long initialDelayMillis, long periodOrDelayMillis, int timeoutMillis) {
		this.timeoutMillis = timeoutMillis;
		this.executor = executor;
		this.isFixedRate = isFixedRate;
		this.initialDelayMillis = initialDelayMillis;
		this.periodOrDelayMillis = periodOrDelayMillis;
		this.collection = Collections.synchronizedCollection(new ArrayList<Wrapper>());
		onServiceStart();
	}

	@Override
	protected void offer(P p, X extra, CompletableFuture<R> cf) {
		BaseGroupedScheduledCompletableFutureService<P, R, X>.Wrapper wrapper = new Wrapper(p, extra, cf);
		collection.add(wrapper);
	}

	@Override
	public void onServiceStart() {
		Runnable command = new Runnable() {
			@Override
			public void run() {
				try {
					if (!collection.isEmpty()) {
						ArrayList<Wrapper> elements = new ArrayList<Wrapper>(collection);
						Iterator<BaseCompletableFutureService<P, R, X>.Wrapper> itr = elements.iterator();
						while (itr.hasNext()) {
							BaseCompletableFutureService<P, R, X>.Wrapper wrapper = itr.next();
							try {
								checkTimeout(wrapper);
							} catch (SaviorException e) {
								itr.remove();
								onFailure(wrapper, e);
							}
						}
						onExecute(elements);
					}
				} catch (Throwable t) {
					logger.debug("Error in pipeline component runnable.  Component=" + this.getClass().getSimpleName(),
							t);
				}
			}
		};
		if (isFixedRate) {
			executor.scheduleAtFixedRate(command, initialDelayMillis, periodOrDelayMillis, TimeUnit.MILLISECONDS);
		} else {
			executor.scheduleWithFixedDelay(command, initialDelayMillis, periodOrDelayMillis, TimeUnit.MILLISECONDS);
		}
	}

	protected abstract void onExecute(Collection<Wrapper> elements);

	protected Collection<P> unwrapParameter(
			Collection<BaseGroupedScheduledCompletableFutureService<P, R, X>.Wrapper> wrappers) {
		ArrayList<P> list = new ArrayList<P>(wrappers.size());
		for (Wrapper w : wrappers) {
			list.add(w.param);
		}
		return list;
	}

	protected void onSuccess(Collection<Wrapper> completedWrappers) {
		for (BaseCompletableFutureService<P, R, X>.Wrapper wrapper : completedWrappers) {
			if (wrapper.result != null) {
				wrapper.future.complete(wrapper.result);
				collection.remove(wrapper);
			}
		}
	}

	protected void onFailure(Wrapper wrapper, Exception e) {
		collection.remove(wrapper);
		super.onFailure(wrapper.param, e, wrapper.future);
	}

	protected void onFailure(Wrapper wrapper) {
		super.onFailure(wrapper.param, wrapper.future);
	}

}
