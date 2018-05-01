package com.ncc.savior.virtueadmin.infrastructure.future;

import java.util.concurrent.CompletableFuture;

/**
 * Simple Untity implementation of {@link BaseCompletableFutureService} for
 * fast, immediate tasks. Concrete implementations should implement
 * {@link #onExecute(Object, Object)} returning the resulting value. Throwing an
 * exception will cause a failure.
 * 
 *
 * @param <P>
 * @param <R>
 * @param <X>
 */
public abstract class BaseImediateCompletableFutureService<P, R, X> extends BaseCompletableFutureService<P, R, X> {

	private String serviceName = null;

	public BaseImediateCompletableFutureService(String serviceName) {
		this.serviceName = serviceName;
	}

	@Override
	protected void offer(P p, X extra, CompletableFuture<R> cf) {
		try {
			R result = onExecute(p, extra);
			onSuccess(result, cf);
		} catch (Exception e) {
			onFailure(p, e, cf);
		}
	}

	protected abstract R onExecute(P param, X extra);

	@Override
	public void onServiceStart() {
		// do nothing
	}

	@Override
	protected String getServiceName() {
		return serviceName;
	}

}
