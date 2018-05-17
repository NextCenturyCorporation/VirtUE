package com.ncc.savior.desktop.clipboard.connection;

import java.io.Closeable;
import java.io.InputStream;
import java.io.OutputStream;

public interface IConnectionWrapper extends Closeable {

	OutputStream getOutputStream();

	InputStream getInputStream();

}
