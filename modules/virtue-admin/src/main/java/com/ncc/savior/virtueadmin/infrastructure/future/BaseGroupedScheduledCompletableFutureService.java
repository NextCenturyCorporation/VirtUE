package com.ncc.savior.virtueadmin.infrastructure.future;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.elasticmapreduce.util.ResizeJobFlowStep.OnFailure;

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
			long initialDelayMillis, long periodOrDelayMillis) {
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
