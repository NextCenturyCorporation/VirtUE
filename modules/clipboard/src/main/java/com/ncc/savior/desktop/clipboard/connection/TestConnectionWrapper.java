package com.ncc.savior.desktop.clipboard.connection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestConnectionWrapper implements IConnectionWrapper {
	private static final Logger logger = LoggerFactory.getLogger(TestConnectionWrapper.class);
	private IConnectionWrapper internal;
	private OutputStream os;
	private InputStream is;

	public TestConnectionWrapper(IConnectionWrapper connectionWrapper) throws IOException {
		
		this.internal = connectionWrapper;
		InputStream iis = internal.getInputStream();
		OutputStream ios = internal.getOutputStream();
		os = new OutputStream() {

			@Override
			public void write(int b) throws IOException {
				ios.write(b);
				char c = (char) b;
				logger.info("" + c);
			}
		};
		is = new InputStream() {

			@Override
			public int read() throws IOException {
				int a = iis.read();
				char b = (char) a;
				logger.info("" + b);
				return a;
			}
		};
	}

	@Override
	public void close() throws IOException {
		if (internal != null) {
			internal.close();
		}
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		return os;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return is;
	}

}
