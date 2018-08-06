package com.ncc.savior.desktop.clipboard;

import java.io.IOException;

import com.ncc.savior.desktop.xpra.connection.ssh.SshConnectionFactory.SshConnectionParameters;

/**
 * Interface to handle connections to clipboard bridges.
 * 
 *
 */
public interface IClipboardManager {

	/**
	 * connects clipboard and returns an id that can be used to close the
	 * connection.
	 * 
	 * @param params
	 * @param groupId
	 * @param groupId
	 * @return
	 * @throws IOException
	 */
	String connectClipboard(SshConnectionParameters params, String displayName, String groupId) throws IOException;

	/**
	 * Closes the clipboard connection for the given clipboard Id
	 * 
	 * @param clipboardId
	 * @throws IOException
	 */
	void closeConnection(String clipboardId) throws IOException;

}
