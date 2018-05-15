package com.ncc.savior.desktop.clipboard.hub;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocketFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClipboardHub {
	private static final Logger logger = LoggerFactory.getLogger(ClipboardHub.class);

	public static void main(String[] args) throws IOException, ClassNotFoundException {
		ServerSocketFactory sFactory = SSLServerSocketFactory.getDefault();
		ServerSocket server = sFactory.createServerSocket(1022);
		Socket socket = server.accept();
		logger.debug("got socket");
		ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
		ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
		Object o = null;
		try {
			while ((o = in.readObject()) != null) {
				logger.info("read: " + o);
			}
		} catch (EOFException e) {
			// expected
		}

		// ServerSocketListener<Client> ssl = new ServerSocketListener
	}

}
