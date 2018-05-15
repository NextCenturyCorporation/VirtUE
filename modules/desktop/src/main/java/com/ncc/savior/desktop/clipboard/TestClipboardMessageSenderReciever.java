package com.ncc.savior.desktop.clipboard;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.clipboard.data.PlaintTextClipboardData;
import com.ncc.savior.desktop.clipboard.hub.ClipboardHub;
import com.ncc.savior.desktop.clipboard.messages.ClipboardDataMessage;
import com.ncc.savior.desktop.clipboard.messages.ClipboardDataRequest;
import com.ncc.savior.desktop.clipboard.messages.IClipboardMessage;

/**
 * allows local code to somewhat simulate a {@link ClipboardHub}
 *
 *
 */
public class TestClipboardMessageSenderReciever extends BaseClipboardMessageSenderReciever {
	private static Logger logger = LoggerFactory.getLogger(TestClipboardMessageSenderReciever.class);
	private String testClientId;

	public TestClipboardMessageSenderReciever(String testClientId) {
		this.testClientId = testClientId;
	}

	@Override
	public void sendMessageToHub(IClipboardMessage message) {
		logger.debug("Message send to hub " + message);
		if (message instanceof ClipboardDataRequest) {
			testRecieveMessage(
					new ClipboardDataMessage(testClientId, new PlaintTextClipboardData(new Date().toString())));
		}
	}

	public void testRecieveMessage(IClipboardMessage message) {
		logger.debug("recieved message from hub=" + message);
		this.clipboardMessageHandler.onMessage(message);
	}
}
