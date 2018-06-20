package com.ncc.savior.virtueadmin.infrastructure.future;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.util.SaviorException;

/**
 * Base class for services services that may be asynchronous and use
 * {@link CompletableFuture}s to indicate completion and pass parameters.
 * 
 * These services help to chain futures together as well as schedule tasks that
 * should occur intermittently until successful.
 * 
 * Implementers are expected to implement
 * {@link #offer(Object, Object, CompletableFuture)} calls and need to call
 * {@link #onSuccess(Object, CompletableFuture)} or
 * {@link #onFailure(Object, CompletableFuture)} or
 * {@link #onFailure(Object, Exception, CompletableFuture)}.
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

	public BaseCompletableFutureService() {
	}

	public CompletableFuture<R> startFutures(P param, X extra) {
		CompletableFuture<P> future = CompletableFuture.completedFuture(param);
		return chainFutures(future, extra);
	}

	/**
	 * Adds this service on a future chain. When priorCf completes, this service
	 * will execute on the parameter from the prior {@link CompletableFuture} with
	 * whatever data is in the extra parameter in the function call. It will return
	 * a {@link CompletableFuture} which can be used to chain another service or
	 * execute another action on completion.
	 * 
	 * @param priorCf
	 * @param extra
	 * @return
	 */
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

	/**
	 * Function call to indicate that a parameter is ready and should be acted upon
	 * or setup to be acted upon later. Once complete, the future should be
	 * completed.
	 * 
	 * @param t
	 * @param extra
	 * @param cf
	 */
	protected abstract void offer(P t, X extra, CompletableFuture<R> cf);

	protected void onSuccess(R result, CompletableFuture<R> cf) {
		if (logger.isTraceEnabled()) {
			logger.trace(getServiceName() + " succeeded with result" + result);
		}
		cf.complete(result);
	}

	protected abstract String getServiceName();

	protected void onFailure(P initial, Exception e, CompletableFuture<R> cf) {
		if (logger.isDebugEnabled()) {
			logger.debug(getServiceName() + " failed with initial data=" + initial + " and error:", e);
		}
		cf.completeExceptionally(e);
	}

	protected void onFailure(P initial, CompletableFuture<R> cf) {
		SaviorException se = new SaviorException(SaviorException.UNKNOWN_ERROR, "Unknown error with value " + initial);
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
