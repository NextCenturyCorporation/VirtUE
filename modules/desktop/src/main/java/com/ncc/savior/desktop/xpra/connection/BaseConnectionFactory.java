package com.ncc.savior.desktop.xpra.connection;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseConnectionFactory {
    private static Logger logger = LoggerFactory.getLogger(BaseConnectionFactory.class);
	private ConnectListenerManager listenerManager = new ConnectListenerManager();


    public IConnection connect(IConnectionParameters params) {
        try {
			listenerManager.onBeforeConnectionAttempt(params);
        } catch (Exception e) {
            logger.warn("Error attempting to send beforeConnectionAttempt event!", e);
        }
        IConnection connection = null;
        try {
            connection = doConnect(params);
        } catch (IOException e) {
            try {
				listenerManager.onConnectionFailure(params, e);
            } catch (Exception e2) {
                logger.warn("Error attempting to send onConnectionFailure event!", e2);
            }
        }

        try {
			listenerManager.onConnectionSuccess(connection);
        } catch (Exception e) {
            logger.warn("Error attempting to send onConnectionSuccess event!", e);
        }
        return connection;
    }

	public void addListener(IConnectListener listener) {
		listenerManager.addListener(listener);
	}

	public void removeListener(IConnectListener listener) {
		listenerManager.removeListener(listener);
	}

    protected abstract IConnection doConnect(IConnectionParameters params) throws IOException;
}
