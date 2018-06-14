package com.ncc.savior.desktop.clipboard.connection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.ncc.savior.desktop.clipboard.client.StandardInOutClipboardClient;

/**
 * Implementation of {@link IConnectionWrapper} that uses a {@link JSch}
 * {@link Channel}. These are often created via connecting to a remote machine
 * via Jsch's implementation of SSH and are used with
 * {@link StandardInOutClipboardClient}s and {@link StandardInOutConnection} on
 * the remote machine.
 * 
 *
 */
public class JschChannelConnectionWrapper implements IConnectionWrapper {

	private Channel channel;

	public JschChannelConnectionWrapper(Channel ch) {
		this.channel = ch;
	}

	@Override
	public void close() throws IOException {
		if (channel != null) {
			channel.disconnect();
		}
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		return channel.getOutputStream();
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return channel.getInputStream();
	}

}
