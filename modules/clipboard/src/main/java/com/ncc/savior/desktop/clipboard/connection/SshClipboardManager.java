package com.ncc.savior.desktop.clipboard.connection;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.ncc.savior.desktop.clipboard.IClipboardManager;
import com.ncc.savior.desktop.clipboard.IClipboardWrapper;
import com.ncc.savior.desktop.clipboard.client.ClipboardClient;
import com.ncc.savior.desktop.clipboard.hub.ClipboardHub;
import com.ncc.savior.desktop.clipboard.serialization.IMessageSerializer;
import com.ncc.savior.desktop.clipboard.serialization.LocalSerializationProvider;
import com.ncc.savior.desktop.clipboard.serialization.LocalSerializationProvider.SerializerContainer;
import com.ncc.savior.desktop.xpra.connection.ssh.JschUtils;
import com.ncc.savior.desktop.xpra.connection.ssh.SshConnectionFactory.SshConnectionParameters;
import com.ncc.savior.util.SshUtil;

public class SshClipboardManager implements IClipboardManager {
	private static final String CLIENT_GROUP_ID = "client-group-id";

	private static final Logger logger = LoggerFactory.getLogger(SshClipboardManager.class);

	private ClipboardHub clipboardHub;
	private String command;

	private String sourceJarPath;

	private String destinationFilePath;

	private String clipboardMainClass;

	private String testParam = null;

	private ClipboardClient localClipboardClient;

	public SshClipboardManager(ClipboardHub clipboardHub, String sourceJarPath) {
		this.clipboardHub = clipboardHub;
		this.sourceJarPath = sourceJarPath;
		this.destinationFilePath = "./clipboard.jar";
		this.clipboardMainClass = "com.ncc.savior.desktop.clipboard.client.StandardInOutClipboardClient";
		this.command = "java -cp " + destinationFilePath + " " + clipboardMainClass;
		if (testParam != null) {
			command += " " + testParam;
		}
		if (localClipboardClient == null) {
			// connectLocalClient();
		}
	}

	private void connectLocalClient() {
		SerializerContainer pair = LocalSerializationProvider.createSerializerPair();
		IMessageSerializer localHubSerializer = pair.serializerA;
		IClipboardWrapper clipboardWrapper = ClipboardClient.getClipboardWrapperForOperatingSystem(false);
		IMessageSerializer localClientSerializer = pair.serializerB;
		try {
			this.clipboardHub.addClient(CLIENT_GROUP_ID, localHubSerializer);
			this.localClipboardClient = new ClipboardClient(localClientSerializer, clipboardWrapper);
		} catch (IOException e) {
			// TODO user message?
			logger.error("Local clipboard client initialization failed");
		}
	}

	@Override
	public void connectClipboard(SshConnectionParameters params, String groupId) throws JSchException {

		// TODO figure out the right way to handle errors here
		try {
			Session session = JschUtils.getUnconnectedSession(params);
			session.connect();
			copyClipboardClientIfNeeded(session);
			// need to get correct display!
			connectionClient(session, groupId, params.getDisplay());
		} catch (IOException | SftpException e) {
			logger.error("Error connecting clipboard!", e);
			// TODO fix error handling
			throw new RuntimeException(e);
		}
	}

	private void connectionClient(Session session, String groupId, int display) throws JSchException, IOException {
		String myCommand = "export DISPLAY=:" + display + "; " + command;
		// myCommand = command;
		logger.debug("clipboard command:" + myCommand);
		ChannelExec channel = SshUtil.getChannelFromCommand(session, myCommand);
		if (testParam != null) {
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(channel.getOutputStream()));
			BufferedReader reader = new BufferedReader(new InputStreamReader(channel.getInputStream()));
			writer.write("test");
			writer.newLine();
			writer.flush();
			String line;
			while ((line = reader.readLine()) != null) {
				System.out.println("##" + line);
				writer.write("test");
				writer.newLine();
				writer.flush();
			}
		} else {
			IConnectionWrapper connectionWrapper = new JschChannelConnectionWrapper(channel);
			// connectionWrapper = new TestConnectionWrapper(connectionWrapper);
			IMessageSerializer serializer = IMessageSerializer.getDefaultSerializer(connectionWrapper);
			clipboardHub.addClient(groupId, serializer);
		}

	}

	private void copyClipboardClientIfNeeded(Session session)
			throws FileNotFoundException, SftpException, JSchException {
		FileInputStream fis = new FileInputStream(new File(sourceJarPath));
		SshUtil.sftpFile(session, fis, destinationFilePath);

	}

}
