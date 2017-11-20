package com.ncc.savior.desktop.xpra.connection;

import java.io.IOException;

/**
 * This interface should be implemented and passed to a
 * {@link ConnectListenerManager}. {@link ConnectListenerManager}s are passed to
 * connection factories when connecting
 *
 *
 */
public interface IConnectListener {
	/**
	 * Called before a connection is attempted.
	 * 
	 * @param parameters
	 */
	public void onBeforeConnectAttempt(IConnectionParameters parameters);

	/**
	 * Called when a connection attempt succeeds.
	 * 
	 * @param connection
	 */
	public void onConnectSuccess(IConnection connection);

	/**
	 * Called when a connection attempt fails.
	 * 
	 * @param params
	 * @param e
	 */
	public void onConnectFailure(IConnectionParameters params, IOException e);
}
