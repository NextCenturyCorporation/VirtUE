package com.ncc.savior.desktop.clipboard.connection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
/**
* Implementation of IConnectionWrapper that uses Standard in and standard out (System.in and System.out).
* Used on remote machines for clipboard when an SSH connection has started the application.  Care must
* Be taken to ensure nothing else reads or writes to standard in or standard out.
*/
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
