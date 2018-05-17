package com.ncc.savior.desktop.clipboard;

import java.io.IOException;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.clipboard.data.PlainTextClipboardData;
import com.ncc.savior.desktop.clipboard.hub.ClipboardHub;
import com.ncc.savior.desktop.clipboard.messages.ClipboardDataMessage;
import com.ncc.savior.desktop.clipboard.messages.ClipboardDataRequestMessage;
import com.ncc.savior.desktop.clipboard.messages.IClipboardMessage;

/**
 * allows local code to somewhat simulate a {@link ClipboardHub}
 *
 *
 */
public class TestClipboardMessageSenderReceiver implements IClipboardMessageSenderReceiver {
	private static Logger logger = LoggerFactory.getLogger(TestClipboardMessageSenderReceiver.class);
	private String testClientId;
	private IClipboardMessageHandler clipboardMessageHandler;

	public TestClipboardMessageSenderReceiver(String testClientId) {
		this.testClientId = testClientId;
	}

	@Override
	public void sendMessageToHub(IClipboardMessage message) {
		logger.debug("Message send to hub " + message);
		if (message instanceof ClipboardDataRequestMessage) {
			testReceiveMessage(
					new ClipboardDataMessage(testClientId, new PlainTextClipboardData(new Date().toString()),
							((ClipboardDataRequestMessage) message).getRequestId(), message.getSourceId()));
		}
	}

	public void testReceiveMessage(IClipboardMessage message) {
		logger.debug("received message from hub=" + message);
		this.clipboardMessageHandler.onMessage(message);
	}

	public static void main(String[] args) throws InterruptedException {
	}

	@Override
	public String init() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isValid() {
		// TODO Auto-generated method stub
		return true;
	}
}
