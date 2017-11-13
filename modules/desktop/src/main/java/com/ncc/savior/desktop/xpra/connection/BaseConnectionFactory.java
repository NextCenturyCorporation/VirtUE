package com.ncc.savior.desktop.xpra.connection;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseConnectionFactory {
    private static Logger logger = LoggerFactory.getLogger(BaseConnectionFactory.class);


	public IConnection connect(IConnectionParameters params, ConnectListenerManager listenerManager) {
        try {
			listenerManager.onBeforeConnectAttempt(params);
        } catch (Exception e) {
            logger.warn("Error attempting to send beforeConnectionAttempt event!", e);
        }
        IConnection connection = null;
        try {
            connection = doConnect(params);
        } catch (IOException e) {
            try {
				listenerManager.onConnectFailure(params, e);
            } catch (Exception e2) {
                logger.warn("Error attempting to send onConnectionFailure event!", e2);
            }
        }

        try {
			listenerManager.onConnectSuccess(connection);
        } catch (Exception e) {
            logger.warn("Error attempting to send onConnectionSuccess event!", e);
        }
        return connection;
    }

    protected abstract IConnection doConnect(IConnectionParameters params) throws IOException;
}
