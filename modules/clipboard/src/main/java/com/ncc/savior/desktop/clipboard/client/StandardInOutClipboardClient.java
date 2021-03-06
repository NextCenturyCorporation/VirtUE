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
package com.ncc.savior.desktop.clipboard.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.clipboard.IClipboardWrapper;
import com.ncc.savior.desktop.clipboard.connection.IConnectionWrapper;
import com.ncc.savior.desktop.clipboard.connection.StandardInOutConnection;
import com.ncc.savior.desktop.clipboard.serialization.IMessageSerializer;

/**
 * Client application that uses standard input and standard output to send
 * clipboard messages. Care must be taken to ensure no classes read System.in or
 * write to System.out, including loggers.
 * 
 * This class is mainly used with SSH connections to remote machines.
 * 
 *
 */
public class StandardInOutClipboardClient {
	private static final Logger logger = LoggerFactory.getLogger(StandardInOutClipboardClient.class);

	public static void main(String[] args) throws IOException, InterruptedException {
		try {
			if (args.length > 0) {
				if (args[0].equals("test")) {
					runTest();
				}
				usage("No Parameters allowed");
			}
			IClipboardWrapper clipboardWrapper = ClipboardClient.getClipboardWrapperForOperatingSystem(true);

			IConnectionWrapper connection = new StandardInOutConnection();
			IMessageSerializer serializer = IMessageSerializer.getDefaultSerializer(connection);
			ClipboardClient client = new ClipboardClient(serializer, clipboardWrapper, true);
			client.initRemoteClient();
			client.waitUntilStopped();
			client.close();

		} catch (Throwable t) {
			logger.error("App stopped by error", t);
		}
		System.exit(0);
	}

	private static void runTest() throws IOException {
		new File("./testTouch.del").createNewFile();
		while (true) {
			logger.debug("started");
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(System.out));
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			logger.debug("about to read");
			String line;
			while ((line = reader.readLine()) != null) {
				logger.debug("reading line: " + line);
				writer.write("from client");
				writer.newLine();
				writer.flush();
			}
		}
	}

	private static void usage(String string) {
		if (string != null) {
			logger.info("Error: " + string);
		}
		logger.info("Usage: executable");
	}
}
