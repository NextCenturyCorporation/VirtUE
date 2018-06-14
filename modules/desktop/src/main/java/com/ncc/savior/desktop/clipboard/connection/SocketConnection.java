package com.ncc.savior.desktop.clipboard.connection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import com.ncc.savior.util.JavaUtil;

/**
 * Specific implementation of {@link IConnectionWrapper} that is based on a
 * {@link Socket}.
 *
 */
public class SocketConnection implements IConnectionWrapper {

	private Socket socket;

	public SocketConnection(Socket clientSocket) throws IOException {
		this.socket = clientSocket;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return socket.getInputStream();
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		return socket.getOutputStream();
	}

	@Override
	public void close() {
		JavaUtil.closeIgnoreErrors(socket);
	}
}
