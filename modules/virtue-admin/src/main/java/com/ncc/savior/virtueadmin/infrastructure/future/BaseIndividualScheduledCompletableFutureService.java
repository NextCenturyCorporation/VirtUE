package com.ncc.savior.virtueadmin.infrastructure.future;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.util.SaviorException;

/**
 * Implementation of {@link BaseCompletableFutureService} where multiple
 * parameters P can NOT be executed at the same time and must be executed
 * individually. In this variant, this there will be a {@link ScheduledFuture}
 * for each passed instance which will call {@link #onExecute(Collection)} of
 * the concrete implementation.
 * 
 * The concrete implementation is expected to implement
 * {@link #onExecute(Collection)} and call either
 * {@link #onSuccess(String, Object, CompletableFuture)} or one of the
 * {@link #onFailure(String, Object, CompletableFuture)}
 * {@link #onFailure(String, Object, Exception, CompletableFuture)} methods
 * provided here. They should not call any onFailure methods provided by the
 * super class {@link BaseCompletableFutureService}
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

public abstract class BaseIndividualScheduledCompletableFutureService<P, R, X>
		extends BaseCompletableFutureService<P, R, X> {
	private static final Logger logger = LoggerFactory.getLogger(BaseIndividualScheduledCompletableFutureService.class);
	private ScheduledExecutorService executor;
	private boolean isFixedRate;
	private long initialDelayMillis;
	private long periodOrDelayMillis;
	private Map<String, ScheduledFuture<?>> futureMap;

	public BaseIndividualScheduledCompletableFutureService(ScheduledExecutorService executor, boolean isFixedRate,
			long initialDelayMillis, long periodOrDelayMillis, int timeoutMillis) {
		this.executor = executor;
		this.isFixedRate = isFixedRate;
		this.initialDelayMillis = initialDelayMillis;
		this.periodOrDelayMillis = periodOrDelayMillis;
		this.futureMap = Collections.synchronizedMap(new HashMap<String, ScheduledFuture<?>>());
		this.timeoutMillis = timeoutMillis;
	}

	@Override
	protected void offer(P p, X extra, CompletableFuture<R> cf) {
		BaseGroupedScheduledCompletableFutureService<P, R, X>.Wrapper wrapper = new Wrapper(p, extra, cf);
		String id = getId(wrapper);
		Runnable command = getRunnable(wrapper, id);
		ScheduledFuture<?> future = schedule(command);
		futureMap.put(id, future);
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
			super.onFailure(null, cf);
		} else {
			future.cancel(true);
			super.onSuccess(result, cf);
		}
	}

	protected void onFailure(String id, P initial, Exception e, CompletableFuture<R> cf) {
		ScheduledFuture<?> future = futureMap.remove(id);
		if (future != null) {
			future.cancel(false);
		}
		super.onFailure(initial, e, cf);
	}

	protected void onFailure(String id, P initial, CompletableFuture<R> cf) {
		ScheduledFuture<?> future = futureMap.remove(id);
		if (future != null) {
			future.cancel(false);
		}
		super.onFailure(initial, cf);
	}

	protected Runnable getRunnable(BaseCompletableFutureService<P, R, X>.Wrapper wrapper, String id) {
		Runnable command = new Runnable() {
			@Override
			public void run() {
				try {
					checkTimeout(wrapper);
					onExecute(id, wrapper);
				} catch (SaviorException e) {
					onFailure(id, wrapper.param, e, wrapper.future);
				} catch (Throwable t) {
					logger.debug("Error in pipeline component runnable.  Component=" + this.getClass().getSimpleName(),
							t);
				}
			}
		};
		return command;
	}

	protected abstract void onExecute(String id, BaseCompletableFutureService<P, R, X>.Wrapper wrapper);

	@Override
	public void onServiceStart() {
		// do nothing
	}
}
