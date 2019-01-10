package com.ncc.savior.desktop.clipboard;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.clipboard.client.ClipboardClient;
import com.ncc.savior.desktop.clipboard.connection.IConnectionWrapper;
import com.ncc.savior.desktop.clipboard.connection.SocketConnection;
import com.ncc.savior.desktop.clipboard.data.ClipboardData;
import com.ncc.savior.desktop.clipboard.data.PlainTextClipboardData;
import com.ncc.savior.desktop.clipboard.guard.ConstantDataGuard;
import com.ncc.savior.desktop.clipboard.hub.ClipboardHub;
import com.ncc.savior.desktop.clipboard.serialization.IMessageSerializer;
import com.ncc.savior.desktop.clipboard.serialization.JavaObjectMessageSerializer;
import com.ncc.savior.util.JavaUtil;

/**
 * Local test class that creates a HUB, a client connected to the location
 * windows system, and a test client which periodically copies and pastes.
 *
 *
 */
public class ClipboardHubLocalTester {

	public static void main(String[] args) throws IOException {
		ServerSocket serverSocket = null;
		try {
			int port = 10022;
			serverSocket = new ServerSocket(port);
			IClipboardWrapper wcw = ClipboardClient.getClipboardWrapperForOperatingSystem(true);
			ClipboardHub hub = new ClipboardHub(new ConstantDataGuard(true));

			Thread clientThread = createClientThread(port, wcw);
			clientThread.start();
			Socket socket = serverSocket.accept();
			IConnectionWrapper connection = new SocketConnection(socket);
			IMessageSerializer serializer = IMessageSerializer.getDefaultSerializer(connection);
			String groupId = "client1";
			hub.addClient(groupId, serializer, "test-1");

			Thread testThread = createClientThread(port, new TestClipboardWrapper());
			testThread.start();
			Socket testSocket = serverSocket.accept();
			IConnectionWrapper testConnection = new SocketConnection(testSocket);
			IMessageSerializer testSerializer = new JavaObjectMessageSerializer(testConnection);
			groupId = "client2";
			hub.addClient(groupId, testSerializer, "test-2");

			while (true) {
				JavaUtil.sleepAndLogInterruption(1000);
			}
		} finally {
			if (serverSocket != null) {
				serverSocket.close();
			}
		}
	}

	private static Thread createClientThread(int port, IClipboardWrapper clipboardWrapper) {
		Thread clientThread = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					Socket clientSocket = new Socket("localhost", port);
					IConnectionWrapper connection = new SocketConnection(clientSocket);
					IMessageSerializer serializer = IMessageSerializer.getDefaultSerializer(connection);
					Thread.sleep(1000);

					@SuppressWarnings({ "unused", "resource" }) // ignore due to test nature of this class
					ClipboardClient client = new ClipboardClient(serializer, clipboardWrapper);
					client.initRemoteClient();
					while (true) {
						// hold
						JavaUtil.sleepAndLogInterruption(1000);
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}, "client");
		return clientThread;
	}

	public static class TestClipboardWrapper implements IClipboardWrapper {
		private static final Logger logger = LoggerFactory.getLogger(TestClipboardWrapper.class);

		private ArrayList<ClipboardFormat> renderFormats;
		private IClipboardListener listener;
		// private ClipboardData data;
		private Thread thread;

		private boolean owner = false;

		protected boolean stopThread;

		public TestClipboardWrapper() {
			this.renderFormats = new ArrayList<ClipboardFormat>();
			this.thread = new Thread(new Runnable() {

				@Override
				public void run() {
					while (!stopThread) {
						JavaUtil.sleepAndLogInterruption(10000);
						logger.debug("test client sending copy");
						copy();
						JavaUtil.sleepAndLogInterruption(10000);
						logger.debug("test client sending paste");
						paste();

					}
				}

			}, "TestClipboard");
			this.thread.start();
		}

		protected void copy() {
			Set<ClipboardFormat> formats = Collections.singleton(ClipboardFormat.TEXT);
			if (listener != null) {
				listener.onClipboardChanged(formats);
			}
			owner = true;
		}

		private void paste() {
			if (!owner && listener != null) {
				ClipboardFormat format = ClipboardFormat.TEXT;
				listener.onPasteAttempt(format);
			}
		}

		@Override
		public void setDelayedRenderFormats(Set<ClipboardFormat> formats) {
			owner = false;
			this.renderFormats.clear();
			this.renderFormats.addAll(formats);
		}

		@Override
		public void setClipboardListener(IClipboardListener listener) {
			this.listener = listener;
		}

		@Override
		public void setDelayedRenderData(ClipboardData clipboardData) {
			logger.info("Pasted data: " + clipboardData);
			// this.data = clipboardData;
		}

		@Override
		public ClipboardData getClipboardData(ClipboardFormat format) {
			return new PlainTextClipboardData(new Date().toString());
		}

		@Override
		public void close() throws IOException {
			stopThread = true;
		}

	}
}
