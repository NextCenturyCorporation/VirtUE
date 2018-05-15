package com.ncc.savior.desktop.clipboard.client;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Set;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.clipboard.IClipboardMessageHandler;
import com.ncc.savior.desktop.clipboard.IClipboardMessageSenderReceiver;
import com.ncc.savior.desktop.clipboard.IClipboardWrapper;
import com.ncc.savior.desktop.clipboard.IClipboardWrapper.IClipboardListener;
import com.ncc.savior.desktop.clipboard.messages.ClipboardChangedMessage;
import com.ncc.savior.desktop.clipboard.messages.ClipboardDataMessage;
import com.ncc.savior.desktop.clipboard.messages.ClipboardDataRequestMessage;
import com.ncc.savior.desktop.clipboard.messages.IClipboardMessage;

/**
 * Clipboard class to be run on a client machine that we want to connect the
 * clipboard to a networked hub.
 *
 *
 */
public class ClipboardClient {
	private static final Logger logger = LoggerFactory.getLogger(ClipboardClient.class);
	private IClipboardMessageSenderReceiver messenger;
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
	public ClipboardClient(String myClipboardId, IClipboardMessageSenderReceiver messenger,
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
				ClipboardClient.this.messenger.sendMessageToHub(new ClipboardDataRequestMessage(myId, format));
			}

			@Override
			public void onClipboardChanged(Set<Integer> formats) {
				ClipboardClient.this.messenger.sendMessageToHub(new ClipboardChangedMessage(myId, formats));
			}
		};
		clipboard.setClipboardListener(listener);
	}

	protected void onClipboardMessage(IClipboardMessage message) {
		logger.debug("got message=" + message);
		if (message instanceof ClipboardChangedMessage) {
			ClipboardChangedMessage m = (ClipboardChangedMessage) message;
			if (!myId.equals(message.getSourceId())) {
				clipboard.setDelayedRenderFormats(m.getFormats());
			}
		} else if (message instanceof ClipboardDataMessage) {
			clipboard.setDelayedRenderData(((ClipboardDataMessage) message).getData());
		}
	}

	protected static void socketTest() throws IOException, UnknownHostException, InterruptedException {
		SocketFactory socketFactory = SSLSocketFactory.getDefault();
		Socket socket = socketFactory.createSocket("localhost", 1022);
		ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
		// ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
		for (int i = 0; i < 100; i++) {
			out.writeObject(i + "asfdfa");
			logger.debug("wrote " + i);
		}
		Thread.sleep(3000);
		socket.close();
	}
}
