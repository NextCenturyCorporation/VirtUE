package com.ncc.savior.desktop.xpra.connection;

public abstract class BaseConnection implements IConnection {

    protected final IConnectionParameters connectionParams;

    protected BaseConnection(IConnectionParameters params) {
        this.connectionParams = params;
    }

    @Override
    public IConnectionParameters getConnectionParameters() {
        return connectionParams;
    }
}
