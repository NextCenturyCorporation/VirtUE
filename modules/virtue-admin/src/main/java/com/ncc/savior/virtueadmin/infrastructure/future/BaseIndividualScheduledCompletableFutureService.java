package com.ncc.savior.virtueadmin.infrastructure.future;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.virtueadmin.util.SaviorException;

public abstract class BaseIndividualScheduledCompletableFutureService<P, R, X>
		extends BaseCompletableFutureService<P, R, X> {
	private static final Logger logger = LoggerFactory.getLogger(BaseIndividualScheduledCompletableFutureService.class);
	private ScheduledExecutorService executor;
	private boolean isFixedRate;
	private long initialDelayMillis;
	private long periodOrDelayMillis;
	private Map<String, ScheduledFuture<?>> futureMap;

	public BaseIndividualScheduledCompletableFutureService(ScheduledExecutorService executor, boolean isFixedRate,
			long initialDelayMillis, long periodOrDelayMillis) {
		super(executor);
		this.executor = executor;
		this.isFixedRate = isFixedRate;
		this.initialDelayMillis = initialDelayMillis;
		this.periodOrDelayMillis = periodOrDelayMillis;
		this.futureMap = Collections.synchronizedMap(new HashMap<String, ScheduledFuture<?>>());
	}

	@Override
	protected void offer(P p, X extra, CompletableFuture<R> cf) {
		BaseGroupedScheduledCompletableFutureService<P, R, X>.Wrapper wrapper = new Wrapper(p, extra, cf);
		Runnable command = getRunnable(wrapper);
		ScheduledFuture<?> future = schedule(command);
		futureMap.put(getId(wrapper), future);
	}

	protected abstract String getId(BaseCompletableFutureService<P, R, X>.Wrapper wrapper);

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

	protected void onSuccess(String id, R result, CompletableFuture<R> cf) {
		ScheduledFuture<?> future = futureMap.remove(id);
		if (future == null) {
			onFailure(id, null, cf);
		} else {
			future.cancel(true);
			super.onSuccess(result, cf);
		}
	}

	protected void onFailure(String id, P initial, Exception e, CompletableFuture<R> cf) {
		futureMap.remove(id).cancel(true);
		super.onFailure(initial, e, cf);
	}

	protected void onFailure(String id, P initial, CompletableFuture<R> cf) {
		futureMap.remove(id).cancel(true);
		super.onFailure(initial, cf);
	}

	protected Runnable getRunnable(BaseCompletableFutureService<P, R, X>.Wrapper wrapper) {
		Runnable command = new Runnable() {
			@Override
			public void run() {
				try {
					onExecute(wrapper);
				} catch (SaviorException e) {
					onFailure(getId(wrapper), wrapper.param, e, wrapper.future);
				} catch (Throwable t) {
					logger.debug("Error in pipeline component runnable.  Component=" + this.getClass().getSimpleName(),
							t);
				}
			}
		};
		return command;
	}

	protected abstract void onExecute(BaseCompletableFutureService<P, R, X>.Wrapper wrapper);

	@Override
	public void onServiceStart() {
		// do nothing
	}
}
