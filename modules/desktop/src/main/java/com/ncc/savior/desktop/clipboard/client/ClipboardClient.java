package com.ncc.savior.desktop.clipboard.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.clipboard.IClipboardMessageHandler;
import com.ncc.savior.desktop.clipboard.IClipboardMessageSenderReciever;
import com.ncc.savior.desktop.clipboard.IClipboardWrapper;
import com.ncc.savior.desktop.clipboard.IClipboardWrapper.IClipboardListener;
import com.ncc.savior.desktop.clipboard.TestClipboardMessageSenderReciever;
import com.ncc.savior.desktop.clipboard.messages.ClipboardChanged;
import com.ncc.savior.desktop.clipboard.messages.ClipboardCleared;
import com.ncc.savior.desktop.clipboard.messages.ClipboardDataMessage;
import com.ncc.savior.desktop.clipboard.messages.ClipboardDataRequest;
import com.ncc.savior.desktop.clipboard.messages.IClipboardMessage;
import com.ncc.savior.desktop.clipboard.windows.WindowsClipboardWrapper;

public class ClipboardClient {
	private static final Logger logger = LoggerFactory.getLogger(ClipboardClient.class);
	private IClipboardMessageSenderReciever messenger;
	private IClipboardWrapper clipboard;
	private String myId;

	/**
	 *
	 * @param myClipboardId
	 *            - String ID for each clipboard connected to a single clipboard
	 *            hub. Clipboard hubs should give each connection their own id.
	 * @param messenger
	 *            - abstraction for sending messages back and forth to clipboard hub
	 * @param clipboard
	 *            - abstraction for clipboard functions.
	 */
	public ClipboardClient(String myClipboardId, IClipboardMessageSenderReciever messenger,
			IClipboardWrapper clipboard) {
		myId = myClipboardId;
		this.messenger = messenger;
		this.clipboard = clipboard;
		messenger.setMessageFromHubHandler(new IClipboardMessageHandler() {
			@Override
			public void onMessage(IClipboardMessage message) {
				onClipboardMessage(message);
			}
		});

		IClipboardListener listener = new IClipboardListener() {

			@Override
			public void onPasteAttempt(int format) {
				// clipboard.setDelayedRenderData(new PlaintTextClipboardData("it works"));
				ClipboardClient.this.messenger.sendMessageToHub(new ClipboardDataRequest(myId, format));
			}

			@Override
			public void onClipboardChanged(Set<Integer> formats) {
				ClipboardClient.this.messenger.sendMessageToHub(new ClipboardChanged(myId, formats));
			}
		};
		clipboard.setClipboardListener(listener);
	}

	protected void onClipboardMessage(IClipboardMessage message) {
		logger.debug("got message=" + message);
		if (message instanceof ClipboardCleared) {

		} else if (message instanceof ClipboardChanged) {
			ClipboardChanged m = (ClipboardChanged) message;
			if (!myId.equals(message.getClipboardOwnerId())) {
				clipboard.setDelayedRenderFormats(m.getFormats());
			}
		} else if (message instanceof ClipboardDataMessage) {
			clipboard.setDelayedRenderData(((ClipboardDataMessage) message).getData());
		}
	}

	public static void main(String[] args) throws UnknownHostException, IOException, InterruptedException {
		// from OS
		WindowsClipboardWrapper windowsClipboardWrapper = new WindowsClipboardWrapper();
		// from connection

		String fakeClient1 = "fakeClient1";
		TestClipboardMessageSenderReciever testMessager = new TestClipboardMessageSenderReciever(fakeClient1);
		// from connection
		String myClipboardId = "TestClient1";



		ClipboardClient client = new ClipboardClient(myClipboardId, testMessager, windowsClipboardWrapper);


		Thread.sleep(500);
		Collection<Integer> formats = new ArrayList<Integer>();
		formats.add(1);
		while (true) {
			testMessager.testRecieveMessage(new ClipboardChanged(fakeClient1, formats));
			Thread.sleep(3000);
		}
	}

	private static void socketTest() throws IOException, UnknownHostException, InterruptedException {
		SocketFactory socketFactory = SSLSocketFactory.getDefault();
		Socket socket = socketFactory.createSocket("localhost", 1022);
		ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
		ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
		for (int i = 0; i < 100; i++) {
			out.writeObject(i + "asfdfa");
			logger.debug("wrote " + i);
		}
		Thread.sleep(3000);
		socket.close();
	}
}
