package com.ncc.savior.virtueadmin.infrastructure.future;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public abstract class BaseImediateCompletableFutureService<P, R, X> extends BaseCompletableFutureService<P, R, X> {

	private String serviceName = null;

	public BaseImediateCompletableFutureService(Executor executor, String serviceName) {
		super(executor);
		this.serviceName = serviceName;
	}

	@Override
	protected void offer(P p, X extra, CompletableFuture<R> cf) {
		R result = onExecute(p, extra);
		onSuccess(result, cf);
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
