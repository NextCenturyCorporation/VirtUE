package com.ncc.savior.desktop.clipboard.connection;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.ncc.savior.desktop.clipboard.client.ClipboardClient;
import com.ncc.savior.desktop.clipboard.hub.ClipboardHub;

/**
 * This inferace abstracts away the connection between the {@link ClipboardHub}
 * and the {@link ClipboardClient}s.
 *
 *
 */
public interface IConnectionWrapper extends Closeable {

	OutputStream getOutputStream() throws IOException;

	InputStream getInputStream() throws IOException;

}
