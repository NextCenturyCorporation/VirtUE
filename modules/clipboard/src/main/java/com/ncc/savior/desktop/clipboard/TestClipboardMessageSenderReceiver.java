/*
 * Copyright (C) 2019 Next Century Corporation
 * 
 * This file may be redistributed and/or modified under either the GPL
 * 2.0 or 3-Clause BSD license. In addition, the U.S. Government is
 * granted government purpose rights. For details, see the COPYRIGHT.TXT
 * file at the root of this project.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 * 
 * SPDX-License-Identifier: (GPL-2.0-only OR BSD-3-Clause)
 */
package com.ncc.savior.desktop.clipboard;

import java.io.IOException;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.clipboard.data.PlainTextClipboardData;
import com.ncc.savior.desktop.clipboard.messages.ClipboardDataMessage;
import com.ncc.savior.desktop.clipboard.messages.ClipboardDataRequestMessage;
import com.ncc.savior.desktop.clipboard.messages.IClipboardMessage;

/**
 * Test only code.
 *
 * Allows local code to somewhat simulate an
 * {@link IClipboardMessageSenderReceiver}
 *
 *
 */
public class TestClipboardMessageSenderReceiver implements IClipboardMessageSenderReceiver {
	private static Logger logger = LoggerFactory.getLogger(TestClipboardMessageSenderReceiver.class);
	private String testClientId;
	private IClipboardMessageHandler clipboardMessageHandler;
	private String groupId;

	public TestClipboardMessageSenderReceiver(String testClientId, String groupId) {
		this.testClientId = testClientId;
		this.groupId = groupId;
	}

	@Override
	public void sendMessageToHub(IClipboardMessage message) {
		logger.debug("Message send to hub " + message);
		if (message instanceof ClipboardDataRequestMessage) {
			testReceiveMessage(new ClipboardDataMessage(testClientId, new PlainTextClipboardData(new Date().toString()),
					((ClipboardDataRequestMessage) message).getRequestId(), message.getSourceId()));
		}
	}

	public void testReceiveMessage(IClipboardMessage message) {
		logger.debug("received message from hub=" + message);
		this.clipboardMessageHandler.onMessage(message, groupId);
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

	@Override
	public String getGroupId() {
		return groupId;
	}

	@Override
	public void waitUntilStopped() {
		// TODO Auto-generated method stub

	}

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub

	}
}
