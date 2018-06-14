package com.ncc.savior.desktop.clipboard;

import java.io.Closeable;

import com.jcraft.jsch.JSchException;
import com.ncc.savior.desktop.xpra.connection.ssh.SshConnectionFactory.SshConnectionParameters;

/**
 * Interface to handle connections to clipboard bridges.
 * 
 *
 */
public interface IClipboardManager {

	/**
	 * connects clipboard and returns an object that will close the connection and
	 * release any resources from it.
	 * 
	 * @param params
	 * @param groupId
	 * @return
	 * @throws JSchException
	 */
	Closeable connectClipboard(SshConnectionParameters params, String groupId) throws JSchException;

}
