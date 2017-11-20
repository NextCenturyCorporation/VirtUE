package com.ncc.savior.desktop.xpra.connection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Single connection to an Xpra server.
 *
 *
 */
public interface IConnection {

	/**
	 * Connection parameters used to create this connection. This can be used to
	 * reconnect or just for informational purposes.
	 *
	 * @return
	 */
	public IConnectionParameters getConnectionParameters();

	public InputStream getInputStream() throws IOException;

	public OutputStream getOutputStream() throws IOException;

	public boolean isActive();
}
