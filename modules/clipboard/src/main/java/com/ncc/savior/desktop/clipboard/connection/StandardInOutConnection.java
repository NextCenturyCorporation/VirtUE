package com.ncc.savior.desktop.clipboard.connection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StandardInOutConnection implements IConnectionWrapper {

	@Override
	public void close() throws IOException {
		// Do nothing
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		return System.out;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return System.in;
	}

}
