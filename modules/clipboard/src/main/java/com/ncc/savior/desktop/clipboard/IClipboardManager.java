package com.ncc.savior.desktop.clipboard;

import com.jcraft.jsch.JSchException;
import com.ncc.savior.desktop.xpra.connection.ssh.SshConnectionFactory.SshConnectionParameters;

public interface IClipboardManager {

	void connectClipboard(SshConnectionParameters params, String groupId) throws JSchException;

}
