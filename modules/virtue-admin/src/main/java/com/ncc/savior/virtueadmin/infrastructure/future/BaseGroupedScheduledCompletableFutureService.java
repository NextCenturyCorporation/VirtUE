package com.ncc.savior.virtueadmin.infrastructure.future;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
		super(executor);
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

}
