package com.ncc.savior.desktop.clipboard.client;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.clipboard.ClipboardFormat;
import com.ncc.savior.desktop.clipboard.IClipboardMessageHandler;
import com.ncc.savior.desktop.clipboard.IClipboardMessageSenderReceiver;
import com.ncc.savior.desktop.clipboard.IClipboardWrapper;
import com.ncc.savior.desktop.clipboard.IClipboardWrapper.IClipboardListener;
import com.ncc.savior.desktop.clipboard.MessageTransmitter;
import com.ncc.savior.desktop.clipboard.data.ClipboardData;
import com.ncc.savior.desktop.clipboard.linux.X11ClipboardWrapper;
import com.ncc.savior.desktop.clipboard.messages.ClipboardChangedMessage;
import com.ncc.savior.desktop.clipboard.messages.ClipboardDataMessage;
import com.ncc.savior.desktop.clipboard.messages.ClipboardDataRequestMessage;
import com.ncc.savior.desktop.clipboard.messages.ClipboardFormatsRequestMessage;
import com.ncc.savior.desktop.clipboard.messages.IClipboardMessage;
import com.ncc.savior.desktop.clipboard.serialization.IMessageSerializer;
import com.ncc.savior.desktop.clipboard.windows.WindowsClipboardWrapper;
import com.ncc.savior.util.JavaUtil;
import com.ncc.savior.virtueadmin.model.OS;

/**
 * Clipboard class to be run on a client machine that we want to connect the
 * clipboard to a networked hub.
 *
 *
 */
public class ClipboardClient {
	private static final Logger logger = LoggerFactory.getLogger(ClipboardClient.class);
	private IClipboardMessageSenderReceiver transmitter;
	private IClipboardWrapper clipboard;
	private String myId;
	private long timeoutMillis = 3000;
	private Map<String, Thread> requestToThread;
	private Map<String, ClipboardData> requestToData;

	/**
	 *
	 * @param serializer
	 *            - abstraction for class that can read and write message objects
	 * @param clipboardWrapper
	 *            - abstraction for clipboard functions.
	 */

	public ClipboardClient(IMessageSerializer serializer, IClipboardWrapper clipboardWrapper) throws IOException {
		this.requestToThread = new TreeMap<String, Thread>();
		this.requestToData = new TreeMap<String, ClipboardData>();
		IClipboardMessageHandler handler = new IClipboardMessageHandler() {
			@Override
			public void onMessage(IClipboardMessage message, String messageHandlerGroupId) {
				onClipboardMessage(message);
			}

			@Override
			public void onMessageError(IOException e) {
				// TODO should we let someone else listen to this event?
				logger.error("Message error.  Client stopped. ", e);

			}
		};
		IClipboardMessageSenderReceiver transmitter = new MessageTransmitter(serializer, handler, "client");
		this.myId = transmitter.init();
		logger.debug("new client created with id=" + myId);
		this.transmitter = transmitter;
		this.clipboard = clipboardWrapper;
		IClipboardListener listener = new IClipboardListener() {

			@Override
			public void onPasteAttempt(ClipboardFormat format) {
				try {
					ClipboardDataRequestMessage requestMsg = new ClipboardDataRequestMessage(myId, format,
							UUID.randomUUID().toString());
					if (logger.isTraceEnabled()) {
						logger.trace("Sending message=" + requestMsg);
					}
					ClipboardClient.this.transmitter.sendMessageToHub(requestMsg);
					ClipboardData clipboardData = blockForClipboardData(requestMsg.getRequestId());
					if (logger.isTraceEnabled()) {
						logger.trace("Setting data to " + clipboardData);
					}
					if (clipboardData != null) {
						clipboard.setDelayedRenderData(clipboardData);
					}
				} catch (IOException e) {
					logger.error("Error pasting data", e);
				}
			}

			@Override
			public void onClipboardChanged(Set<ClipboardFormat> formats) {
				try {
					ClipboardChangedMessage msg = new ClipboardChangedMessage(myId, formats);
					// logger.debug("sending message=" + msg);
					ClipboardClient.this.transmitter.sendMessageToHub(msg);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		clipboardWrapper.setClipboardListener(listener);
		transmitter.sendMessageToHub(new ClipboardFormatsRequestMessage(myId));
	}

	/**
	 * Blocks until the thread is interrupted. It is assumed this will be
	 * interrupted by {@link #storeClipboardData(ClipboardDataMessage)} call. This
	 * is because Windows must put the data on the clipboard using the same thread
	 * that got the DELAYED RENDER requeset.
	 *
	 * @param requestId
	 * @return
	 */
	protected ClipboardData blockForClipboardData(String requestId) {
		requestToThread.put(requestId, Thread.currentThread());
		try {

			if (logger.isTraceEnabled()) {
				logger.trace("blocking for data. request=" + requestId);
			}
			Thread.sleep(timeoutMillis);
		} catch (InterruptedException e) {
			if (logger.isTraceEnabled()) {
				logger.trace("Interrupted! request=" + requestId);
			}
			ClipboardData data = requestToData.remove(requestId);
			if (data == null) {
				logger.error("Error getting data for request=" + requestId);
			}
			// make sure interupt is cleared
			Thread.interrupted();
			return data;
		} finally {
			// clear the maps always
			requestToThread.remove(requestId);
			requestToData.remove(requestId);
		}
		logger.error("Request for data with id=" + requestId + " timedout!");
		return null;
	}

	protected void onClipboardMessage(IClipboardMessage message) {
		if (logger.isTraceEnabled()) {
			logger.trace("got message=" + message);
		}
		if (message instanceof ClipboardChangedMessage) {
			ClipboardChangedMessage m = (ClipboardChangedMessage) message;
			if (!myId.equals(message.getSourceId())) {
				clipboard.setDelayedRenderFormats(m.getFormats());
			}
		} else if (message instanceof ClipboardDataMessage) {
			storeClipboardData((ClipboardDataMessage) message);
		} else if (message instanceof ClipboardDataRequestMessage) {
			ClipboardDataRequestMessage m = ((ClipboardDataRequestMessage) message);
			ClipboardData data = clipboard.getClipboardData(m.getFormat());
			ClipboardDataMessage dataMessage = new ClipboardDataMessage(myId, data, m.getRequestId(), m.getSourceId());
			try {
				if (logger.isTraceEnabled()) {
					logger.trace("sending message=" + dataMessage);
				}
				transmitter.sendMessageToHub(dataMessage);
			} catch (IOException e) {
				logger.error("Error sending data message=" + dataMessage);
			}
		}
	}

	/**
	 * Returns true if the internal transmitter is valid. Internal transmitter is
	 * valid as long as it hasn't throw an error on transmission. Tranmission errors
	 * are assumed to be fatal.
	 *
	 * @return
	 */
	public boolean isValid() {
		return transmitter.isValid();
	}

	/**
	 * blocks until client has stopped listening for messages signally that it is
	 * done or disconnected and should no longer be used.
	 */
	public void waitUntilStopped() {
		transmitter.waitUntilStopped();
	}

	private void storeClipboardData(ClipboardDataMessage message) {
		String reqId = message.getRequestId();
		Thread t = requestToThread.get(reqId);
		if (t != null) {
			if (logger.isTraceEnabled()) {
				logger.trace("storing data.  request=" + reqId);
			}
			requestToData.put(reqId, message.getData());
			t.interrupt();
		} else {
			logger.error("unable to find thread to interrupt for clipboard data");
		}
	}

	public static IClipboardWrapper getClipboardWrapperForOperatingSystem(boolean takeClipboard) {
		OS os = JavaUtil.getOs();
		IClipboardWrapper clipboardWrapper;
		switch (os) {
		case LINUX:
			clipboardWrapper = new X11ClipboardWrapper(takeClipboard);
			break;
		case MAC:
			throw new RuntimeException("Mac clipboard is currently not supported!");
		case WINDOWS:
			clipboardWrapper = new WindowsClipboardWrapper(takeClipboard);
			break;
		default:
			throw new RuntimeException("Clipboard is currently not supported on your operating system!");
		}
		return clipboardWrapper;
	}
}