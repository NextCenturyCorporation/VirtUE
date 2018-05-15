package com.ncc.savior.desktop.clipboard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.clipboard.client.ClipboardClient;
import com.ncc.savior.desktop.clipboard.data.PlaintTextClipboardData;
import com.ncc.savior.desktop.clipboard.hub.ClipboardHub;
import com.ncc.savior.desktop.clipboard.messages.ClipboardChangedMessage;
import com.ncc.savior.desktop.clipboard.messages.ClipboardDataMessage;
import com.ncc.savior.desktop.clipboard.messages.ClipboardDataRequestMessage;
import com.ncc.savior.desktop.clipboard.messages.IClipboardMessage;
import com.ncc.savior.desktop.clipboard.windows.WindowsClipboardWrapper;

/**
 * allows local code to somewhat simulate a {@link ClipboardHub}
 *
 *
 */
public class TestClipboardMessageSenderReceiver extends BaseClipboardMessageSenderReceiver {
	private static Logger logger = LoggerFactory.getLogger(TestClipboardMessageSenderReceiver.class);
	private String testClientId;

	public TestClipboardMessageSenderReceiver(String testClientId) {
		this.testClientId = testClientId;
	}

	@Override
	public void sendMessageToHub(IClipboardMessage message) {
		logger.debug("Message send to hub " + message);
		if (message instanceof ClipboardDataRequestMessage) {
			testReceiveMessage(
					new ClipboardDataMessage(testClientId, new PlaintTextClipboardData(new Date().toString())));
		}
	}

	public void testReceiveMessage(IClipboardMessage message) {
		logger.debug("received message from hub=" + message);
		this.clipboardMessageHandler.onMessage(message);
	}

	public static void main(String[] args) throws InterruptedException {
		runTest1();
	}

	private static void runTest1() throws InterruptedException {
		// from OS
		WindowsClipboardWrapper windowsClipboardWrapper = new WindowsClipboardWrapper();
		// from connection

		String fakeClient1 = "fakeClient1";
		TestClipboardMessageSenderReceiver testMessager = new TestClipboardMessageSenderReceiver(fakeClient1);
		// from connection
		String myClipboardId = "TestClient1";

		ClipboardClient client = new ClipboardClient(myClipboardId, testMessager, windowsClipboardWrapper);

		Thread.sleep(500);
		Collection<Integer> formats = new ArrayList<Integer>();
		formats.add(1);
		while (true) {
			testMessager.testReceiveMessage(new ClipboardChangedMessage(fakeClient1, formats));
			Thread.sleep(3000);
		}
	}
}
