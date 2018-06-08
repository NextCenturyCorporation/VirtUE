package com.ncc.savior.desktop.clipboard.connection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.jcraft.jsch.Channel;

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
