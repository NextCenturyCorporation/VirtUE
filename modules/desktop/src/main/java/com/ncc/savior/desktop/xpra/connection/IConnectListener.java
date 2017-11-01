package com.ncc.savior.desktop.xpra.connection;

import java.io.IOException;

public interface IConnectListener {
	public void onBeforeConnectAttempt(IConnectionParameters parameters);

	public void onConnectSuccess(IConnection connection);

	public void onConnectFailure(IConnectionParameters params, IOException e);
}
