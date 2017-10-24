package com.ncc.savior.desktop.xpra.connection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ConnectListenerManager {
	protected final List<IConnectListener> listeners = new ArrayList<>();

	public void addListener(IConnectListener listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}
	}

	public boolean removeListener(IConnectListener listener) {
		synchronized (listeners) {
			return listeners.remove(listener);
		}
	}

	public void onBeforeConnectionAttempt(IConnectionParameters parameters) {
		synchronized (listeners) {
			for (IConnectListener l : listeners) {
				l.onBeforeConnectAttempt(parameters);
			}
		}
	}

	public void onConnectionSuccess(IConnection conn) {
		synchronized (listeners) {
			for (IConnectListener l : listeners) {
				l.onConnectSuccess(conn);
			}
		}
	}

	public void onConnectionFailure(IConnectionParameters params, IOException e) {
		synchronized (listeners) {
			for (IConnectListener l : listeners) {
				l.onConnectFailure(params, e);
			}
		}
	}
}
