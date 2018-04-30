package com.ncc.savior.virtueadmin.infrastructure.future;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.virtueadmin.util.SaviorException;

/**
 * 
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
public abstract class BaseCompletableFutureService<P, R, X> {
	private static final Logger logger = LoggerFactory.getLogger(BaseCompletableFutureService.class);
	private Executor executor;

	public BaseCompletableFutureService(Executor executor) {
		this.executor = executor;
	}

	public CompletableFuture<R> startFutures(P param, X extra) {
		CompletableFuture<P> future = CompletableFuture.completedFuture(param);
		return chainFutures(future, extra);
	}

	public CompletableFuture<R> chainFutures(CompletableFuture<P> priorCf, X extra) {
		if (logger.isTraceEnabled()) {
			logger.trace("Adding future to " + getServiceName() + " with extra=" + extra);
		}
		CompletableFuture<R> cf = new CompletableFuture<R>();
		priorCf.thenAccept(new Consumer<P>() {

			@Override
			public void accept(P t) {
				if (logger.isTraceEnabled()) {
					logger.trace(getServiceName() + " Offering " + t + " to service internals with extra=" + extra);
				}
				offer(t, extra, cf);
			}
		});

		// TODO handle errors better
		priorCf.exceptionally((ex) -> {
			if (logger.isTraceEnabled()) {
				logger.trace(getServiceName() + " Handling excption " + ex.getMessage());
			}
			cf.completeExceptionally(ex);
			return null;
		});
		return cf;
	}

	protected abstract void offer(P t, X extra, CompletableFuture<R> cf);

	protected void onSuccess(R result, CompletableFuture<R> cf) {
		if (logger.isTraceEnabled()) {
			logger.trace(getServiceName() + " succeeded with result" + result);
		}
		cf.complete(result);
	}

	protected void onFailure(P initial, Exception e, CompletableFuture<R> cf) {
		if (logger.isDebugEnabled()) {
			logger.debug(getServiceName() + " failed with initial data=" + initial + " and error:", e);
		}
		cf.completeExceptionally(e);
	}

	protected abstract String getServiceName();

	protected void onFailure(P initial, CompletableFuture<R> cf) {
		SaviorException se = new SaviorException(SaviorException.UNKNOWN_ERROR, "Unknown error with value" + initial);
		if (logger.isDebugEnabled()) {
			logger.debug(getServiceName() + " failed with initial data=" + initial + " and error:", se);
		}
		cf.completeExceptionally(se);
	}

	public abstract void onServiceStart();

	protected class Wrapper {
		P param;
		X extra;
		CompletableFuture<R> future;
		R result;

		Wrapper(P p, X x, CompletableFuture<R> future) {
			this.param = p;
			this.extra = x;
			this.future = future;
		}
	}
}
