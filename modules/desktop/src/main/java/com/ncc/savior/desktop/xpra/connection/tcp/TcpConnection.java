package com.ncc.savior.desktop.xpra.connection.tcp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import com.ncc.savior.desktop.xpra.connection.BaseConnection;

public class TcpConnection extends BaseConnection {

    protected final Socket socket;

    public TcpConnection(TcpConnectionFactory.TcpConnectionParameters params, Socket socket) {
        super(params);
        this.socket = socket;
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
	public boolean isActive() {
		return socket != null && socket.isConnected() && !socket.isClosed();
	}

	@Override
	public String toString() {
		return "TcpConnection [socket=" + socket + ", connectionParams=" + connectionParams + "]";
	}
}
