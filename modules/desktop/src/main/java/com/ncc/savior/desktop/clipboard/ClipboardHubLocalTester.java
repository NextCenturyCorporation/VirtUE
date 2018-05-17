package com.ncc.savior.desktop.clipboard;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.clipboard.client.ClipboardClient;
import com.ncc.savior.desktop.clipboard.connection.IConnectionWrapper;
import com.ncc.savior.desktop.clipboard.connection.SocketConnection;
import com.ncc.savior.desktop.clipboard.data.ClipboardData;
import com.ncc.savior.desktop.clipboard.data.PlainTextClipboardData;
import com.ncc.savior.desktop.clipboard.hub.ClipboardHub;
import com.ncc.savior.desktop.clipboard.serialization.IMessageSerializer;
import com.ncc.savior.desktop.clipboard.serialization.JavaObjectMessageSerializer;
import com.ncc.savior.desktop.clipboard.windows.WindowsClipboardWrapper;
import com.ncc.savior.util.JavaUtil;

public class ClipboardHubLocalTester {

	public static void main(String[] args) throws IOException {
		int port = 1022;
		ServerSocket serverSocket = new ServerSocket(port);
		WindowsClipboardWrapper wcw = new WindowsClipboardWrapper();
		ClipboardHub hub = new ClipboardHub();

		Thread clientThread = createClientThread(port, wcw);
		clientThread.start();
		Socket socket = serverSocket.accept();
		IConnectionWrapper connection = new SocketConnection(socket);
		IMessageSerializer serializer = new JavaObjectMessageSerializer(connection);
		hub.addClient(serializer);

		Thread testThread = createClientThread(port, new TestClipboardWrapper());
		testThread.start();
		Socket testSocket = serverSocket.accept();
		IConnectionWrapper testConnection = new SocketConnection(testSocket);
		IMessageSerializer testSerializer = new JavaObjectMessageSerializer(testConnection);
		hub.addClient(testSerializer);

		while (true) {
			JavaUtil.sleepAndLogInterruption(1000);
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
					IMessageSerializer serializer = new JavaObjectMessageSerializer(connection);
					Thread.sleep(1000);

					ClipboardClient client = new ClipboardClient(serializer, clipboardWrapper);
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

		private ArrayList<Integer> renderFormats;
		private IClipboardListener listener;
		private ClipboardData data;
		private Thread thread;

		private boolean owner = false;

		public TestClipboardWrapper() {
			this.renderFormats = new ArrayList<Integer>();
			this.thread = new Thread(new Runnable() {

				@Override
				public void run() {
					while (true) {
						copy();
						JavaUtil.sleepAndLogInterruption(3000);
						paste();
						JavaUtil.sleepAndLogInterruption(3000);
					}
				}

			}, "TestClipboard");
			this.thread.start();
		}

		protected void copy() {
			Set<Integer> formats = new HashSet<Integer>(1);
			formats.add(1);
			if (listener != null) {
				listener.onClipboardChanged(formats);
			}
			owner = true;
		}

		private void paste() {
			if (!owner && listener != null) {
				listener.onPasteAttempt(1);
			}
		}

		@Override
		public void setDelayedRenderFormats(Collection<Integer> formats) {
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
			this.data = clipboardData;
		}

		@Override
		public ClipboardData getClipboardData(int format) {
			return new PlainTextClipboardData(new Date().toString());
		}

	}
}
