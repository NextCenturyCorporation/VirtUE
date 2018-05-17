package com.ncc.savior.desktop.clipboard.connection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import com.ncc.savior.util.JavaUtil;

public class SocketConnection implements IConnectionWrapper {

	private InputStream in;
	private OutputStream out;
	private Socket socket;

	public SocketConnection(Socket clientSocket) throws IOException {
		this.socket = clientSocket;
		this.in = clientSocket.getInputStream();
		this.out = clientSocket.getOutputStream();
	}

	@Override
	public InputStream getInputStream() {
		return in;
	}

	@Override
	public OutputStream getOutputStream() {
		return out;
	}

	@Override
	public void close() {
		JavaUtil.closeIgnoreErrors(in, out, socket);
	}
}
