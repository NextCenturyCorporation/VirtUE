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
package com.ncc.savior.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

public class InputStreamConsumer implements Runnable {
	private static final XLogger logger = XLoggerFactory.getXLogger(InputStreamConsumer.class);

	private InputStream input;
	private StringBuilder output;
	private IOException exception = null;

	public InputStreamConsumer(InputStream input, StringBuilder output) {
		this.input = input;
		this.output = output;
	}

	@Override
	public void run() {
		logger.debug("ISC start");
		int bufSize = 4096;
		char[] buffer = new char[bufSize];
		int totalBytes = 0;
		try (InputStreamReader reader = new InputStreamReader(input)) {
			int bytesRead;
			while ((bytesRead = reader.read(buffer)) != -1) {
				totalBytes += bytesRead;
				output.append(buffer, 0, bytesRead);
			}
		} catch (IOException e) {
			logger.debug("ISC got exception: " + e);
			exception = e;
		}
		finally {
			logger.debug("ISC read " + totalBytes + " bytes");
		}
		logger.debug("ISC done");
	}

	public IOException getException() {
		return exception;
	}
}
