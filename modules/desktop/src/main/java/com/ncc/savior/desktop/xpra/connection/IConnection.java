package com.ncc.savior.desktop.xpra.connection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface IConnection {

    public IConnectionParameters getConnectionParameters();

    public InputStream getInputStream() throws IOException;

    public OutputStream getOutputStream() throws IOException;

	public boolean isActive();
}
