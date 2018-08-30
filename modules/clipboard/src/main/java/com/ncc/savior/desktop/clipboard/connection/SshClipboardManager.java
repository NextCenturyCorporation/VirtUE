package com.ncc.savior.desktop.clipboard.connection;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.ncc.savior.desktop.alerting.PlainAlertMessage;
import com.ncc.savior.desktop.alerting.UserAlertingServiceHolder;
import com.ncc.savior.desktop.clipboard.IClipboardManager;
import com.ncc.savior.desktop.clipboard.IClipboardWrapper;
import com.ncc.savior.desktop.clipboard.client.ClipboardClient;
import com.ncc.savior.desktop.clipboard.client.StandardInOutClipboardClient;
import com.ncc.savior.desktop.clipboard.hub.ClipboardHub;
import com.ncc.savior.desktop.clipboard.hub.ClipboardHub.DisconnectListener;
import com.ncc.savior.desktop.clipboard.serialization.IMessageSerializer;
import com.ncc.savior.desktop.clipboard.serialization.LocalSerializationProvider;
import com.ncc.savior.desktop.clipboard.serialization.LocalSerializationProvider.SerializerContainer;
import com.ncc.savior.desktop.xpra.connection.ssh.JschUtils;
import com.ncc.savior.desktop.xpra.connection.ssh.SshConnectionFactory.SshConnectionParameters;
import com.ncc.savior.util.JavaUtil;
import com.ncc.savior.util.SshUtil;
import com.ncc.savior.virtueadmin.model.ClipboardPermission;

/**
 * Implementation of {@link IClipboardManager} that uses SSH (Jsch) to start
 * remote {@link ClipboardClient}s and create the connection's data stream.
 * 
 *
 */
public class SshClipboardManager implements IClipboardManager {
	private int numRetriesAfterSuccess = 5;

	private static final Logger logger = LoggerFactory.getLogger(SshClipboardManager.class);

	private ClipboardHub clipboardHub;
	private String command;

	private String sourceJarPath;

	private String destinationFilePath;

	private String clipboardMainClass;

	private String testParam = null;

	private ClipboardClient localClipboardClient;

	private HashMap<String, ClipboardClientConnectionProperties> propertiesMap;

	private long retryPeriodMillis = 2000;

	public SshClipboardManager(ClipboardHub clipboardHub, String sourceJarPath) {
		this.clipboardHub = clipboardHub;
		this.propertiesMap = new HashMap<String, ClipboardClientConnectionProperties>();
		clipboardHub.setDisconnectListener(new DisconnectListener() {

			@Override
			public void onDisconnect(String clientId) {
				// do nothing
			}

			@Override
			public void onDisconnect(String clientId, IOException e) {
				logger.error("Clipboard client " + clientId + " closed with exception", e);
				onDisconnectWithError(clientId);
			}
		});
		this.sourceJarPath = sourceJarPath;
		this.destinationFilePath = "clipboard.jar";
		this.clipboardMainClass = StandardInOutClipboardClient.class.getCanonicalName();
		String logConfig = "-Dlogback.configurationFile=logback-remote.xml";
		this.command = "java -cp " + destinationFilePath + " " + logConfig + " " + clipboardMainClass;
		if (testParam != null) {
			command += " " + testParam;
		}
		if (localClipboardClient == null) {
			connectLocalClient();
		}
	}

	protected void onDisconnectWithError(String clientId) {
		// TODO new thread?

		if (propertiesMap.containsKey(clientId)) {
			// remove because if we fail to reconnect, we are done.
			ClipboardClientConnectionProperties props = propertiesMap.get(clientId);
			for (int i = 0; i < numRetriesAfterSuccess; i++) {
				logger.warn(
						"Retrying clipboard client connection attempt # " + (i + 1) + " of " + numRetriesAfterSuccess);
				try {
					// if first retry, try immediately.
					if (i > 0) {
						JavaUtil.sleepAndLogInterruption(retryPeriodMillis);
					}
					connectClipboardOnce(props.connectionParameters, props.groupId, props.displayName, props.clientId);
					return;
				} catch (IOException e) {
					logger.warn(
							"Clipboard reconnect attempt # " + (i + 1) + " of " + numRetriesAfterSuccess + " failed.");
				}
			}
			// reconnect failed since we return on success
			logger.error("Clipboard reconnect failed for client=" + clientId);
			PlainAlertMessage pam = new PlainAlertMessage("Clipboard failed",
					"Clipboard connection unable to reconnect after retries");
			try {
				UserAlertingServiceHolder.sendAlert(pam);
			} catch (IOException e) {
				logger.error("Error sending alert", e);
			}
		} else {
			logger.error("Error: Unable to find properties for client=" + clientId + " after disconnected with error.");
		}
	}

	private void connectLocalClient() {
		try {
			SerializerContainer pair = LocalSerializationProvider.createSerializerPair();
			IMessageSerializer localHubSerializer = pair.serializerA;
			IClipboardWrapper clipboardWrapper = ClipboardClient.getClipboardWrapperForOperatingSystem(false);
			IMessageSerializer localClientSerializer = pair.serializerB;
			this.clipboardHub.addClient(ClipboardPermission.DESKTOP_CLIENT_GROUP_ID, localHubSerializer,
					"Local Desktop");
			this.localClipboardClient = new ClipboardClient(localClientSerializer, clipboardWrapper);
		} catch (Exception e) {
			PlainAlertMessage pam = new PlainAlertMessage("Clipboard failed",
					"Local clipboard initialization failed.  Local clipboard will not be connected with virtues.");
			try {
				UserAlertingServiceHolder.sendAlert(pam);
			} catch (IOException e2) {
				logger.error("Error sending alert", e2);
			}
			logger.error("Local clipboard client initialization failed", e);
		}
	}

	@Override
	public String connectClipboard(SshConnectionParameters params, String displayName, String groupId)
			throws IOException {
		Exception lastException = null;
		for (int i = 0; i < numRetriesAfterSuccess; i++) {
			try {
				String id = connectClipboardOnce(params, groupId, displayName, null);
				return id;
			} catch (Exception e) {
				logger.error("Attempt to connect to clipboard failed.  Attempt #" + (i + 1) + " of "
						+ numRetriesAfterSuccess);
				lastException = e;
			}
		}
		throw new IOException(
				"Unable to connect to clipboard after " + numRetriesAfterSuccess + " tries.  Last exception included.",
				lastException);
	}

	@Override
	public void closeConnection(String clipboardId) throws IOException {
		if (propertiesMap.containsKey(clipboardId)) {
			ClipboardClientConnectionProperties props = propertiesMap.remove(clipboardId);
			if (props != null && props.closeable != null) {
				props.closeable.close();
			}
		}
	}

	/**
	 * Attempts to connect the clipboard exactly once. If successful, stores
	 * properties required for reconnect. Otherwise, throws an exception.
	 * 
	 * @param params
	 * @param groupId
	 * @param clientId
	 * @param clientId
	 * @return
	 * @throws IOException
	 */
	private String connectClipboardOnce(SshConnectionParameters params, String groupId, String displayName,
			String clientId) throws IOException {
		if (sourceJarPath != null && new File(sourceJarPath).exists()) {
			try {
				Session session;
				session = JschUtils.getUnconnectedSession(params);
				session.connect();
				copyClipboardClientIfNeeded(session);
				// need to get correct display!
				ClipboardClientConnectionProperties props = connectionClient(session, groupId, params.getDisplay(),
						displayName, clientId);
				props.connectionParameters = params;
				propertiesMap.put(props.clientId, props);
				return props.clientId;
			} catch (JSchException | SftpException e) {
				throw new IOException(e);
			}
		} else {
			logger.warn("Clipboard jar not present.  Clipboard will be disabled");
			// TODO Alert user
			return null;
		}
	}

	/**
	 * 
	 * @param session
	 * @param groupId
	 * @param display
	 * @param clientId
	 *            - id if reconnecting or null if not
	 * @return - returned closeable will close the connection when close is called.
	 * @throws JSchException
	 * @throws IOException
	 */
	private ClipboardClientConnectionProperties connectionClient(Session session, String groupId, int display,
			String displayName, String clientId) throws JSchException, IOException {
		String myCommand = null;
		if (display > 0) {
			myCommand = "export DISPLAY=:" + display + "; " + command;
		} else {
			myCommand = command;
		}
		// myCommand = command;
		if (logger.isTraceEnabled()) {
			logger.trace("clipboard command:" + myCommand);
		}
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
			clientId = clipboardHub.addClient(groupId, serializer, displayName, clientId);
		}
		String cId = clientId;
		@SuppressWarnings("resource") // closed through call to closeConnection()
		Closeable closeable = () -> {
			clipboardHub.disconnectClient(cId);
			channel.disconnect();
			session.disconnect();
		};
		ClipboardClientConnectionProperties props = new ClipboardClientConnectionProperties();
		props.displayName = displayName;
		props.closeable = closeable;
		props.clientId = clientId;
		props.groupId = groupId;
		return props;
	}

	public String sha2hex(byte[] bytesOfMessage)
			throws NoSuchAlgorithmException, UnsupportedEncodingException, IOException {
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		byte[] thedigest = md.digest(bytesOfMessage);
		BigInteger bigInt = new BigInteger(1, thedigest);
		String hashtext = bigInt.toString(16);

		while (hashtext.length() < 32) {
			hashtext = "0" + hashtext;
		}

		return hashtext;
	}

	private void copyClipboardClientIfNeeded(Session session) throws SftpException, JSchException, IOException {
		FileInputStream fis = new FileInputStream(new File(sourceJarPath));
		// List<String> files = SshUtil.sendCommandFromSession(session, "ls");

		// if (files.contains(destinationFilePath) || true) {
		boolean skipHash = true;
		if (!skipHash) {
			List<String> sha256Output = SshUtil.sendCommandFromSession(session, "sha256sum " + destinationFilePath);
			String remoteHash = sha256Output.get(0);
			String localHash = null;
			try {
				localHash = sha2hex(IOUtils.toByteArray(fis));
			} catch (NoSuchAlgorithmException e) {
				logger.error("error with hashing", e);
			}
			logger.debug("local hash=" + localHash);
			if (localHash == null || !remoteHash.contains(localHash)) {
				SshUtil.sftpFile(session, fis, destinationFilePath);
			}
		} else {
			SshUtil.sftpFile(session, fis, destinationFilePath);
		}
	}

	private static class ClipboardClientConnectionProperties {
		public String displayName;
		public Closeable closeable;
		public String clientId;
		public SshConnectionParameters connectionParameters;
		public String groupId;
	}

}
