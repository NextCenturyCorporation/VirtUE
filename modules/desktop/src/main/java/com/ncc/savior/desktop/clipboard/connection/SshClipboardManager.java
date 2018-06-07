package com.ncc.savior.desktop.clipboard.connection;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.ncc.savior.desktop.clipboard.IClipboardManager;
import com.ncc.savior.desktop.clipboard.hub.ClipboardHub;
import com.ncc.savior.desktop.clipboard.serialization.IMessageSerializer;
import com.ncc.savior.desktop.clipboard.serialization.JavaObjectMessageSerializer;
import com.ncc.savior.desktop.xpra.connection.ssh.JschUtils;
import com.ncc.savior.desktop.xpra.connection.ssh.SshConnectionFactory.SshConnectionParameters;
import com.ncc.savior.util.SshUtil;

public class SshClipboardManager implements IClipboardManager {
	private static final Logger logger = LoggerFactory.getLogger(SshClipboardManager.class);

	private ClipboardHub clipboardHub;
	private String command;

	private String sourceJarPath;

	private String destinationFilePath;

	private String clipboardMainClass;

	public SshClipboardManager(ClipboardHub clipboardHub, String sourceJarPath) {
		this.clipboardHub = clipboardHub;
		this.sourceJarPath = sourceJarPath;
		this.destinationFilePath = "./clipboard.jar";
		this.clipboardMainClass = "";
		this.command = "java -jar " + destinationFilePath + " " + clipboardMainClass;
	}

	@Override
	public void connectClipboard(SshConnectionParameters params, String groupId) throws JSchException {
		// TODO figure out the right way to handle errors here
		try {
			Session session = JschUtils.getUnconnectedSession(params);
			session.connect();
			copyClipboardClientIfNeeded(session);
			connectionClient(session, groupId);
		} catch (IOException | SftpException e) {
			logger.error("Error connecting clipboard!", e);
			// TODO fix error handling
			throw new RuntimeException(e);
		}
	}

	private void connectionClient(Session session, String groupId) throws JSchException, IOException {
		ChannelExec channel = SshUtil.getChannelFromCommand(session, command);

		IConnectionWrapper connectionWrapper = new JschChannelConnectionWrapper(channel);
		IMessageSerializer serializer = new JavaObjectMessageSerializer(connectionWrapper);
		clipboardHub.addClient(groupId, serializer);

	}

	private void copyClipboardClientIfNeeded(Session session)
			throws FileNotFoundException, SftpException, JSchException {
		FileInputStream fis = new FileInputStream(new File(sourceJarPath));
		SshUtil.sftpFile(session, fis, destinationFilePath);

	}

}
