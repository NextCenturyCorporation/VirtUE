package com.ncc.savior.desktop.xpra.connection;

import java.io.IOException;

public abstract class BaseConnectListener implements IConnectListener {

	@Override
	public void onBeforeConnectAttempt(IConnectionParameters parameters) {
		// do nothing
	}

	@Override
	public void onConnectSuccess(IConnection connection) {
		// do nothing
	}

	@Override
	public void onConnectFailure(IConnectionParameters params, IOException e) {
		// do nothing
	}
}
