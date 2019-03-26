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
package com.ncc.savior.desktop.virtues;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator.Feature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ncc.savior.desktop.alerting.BaseAlertMessage;
import com.ncc.savior.desktop.alerting.PlainAlertMessage;
import com.ncc.savior.desktop.alerting.UserAlertingServiceHolder;

public class BridgeSensorService {
	private static final Logger logger = LoggerFactory.getLogger(BridgeSensorService.class);

	private ObjectMapper jsonMapper;
	private BlockingQueue<BridgeSensorMessage> messageQueue;
	private BufferedOutputStream os;
	private Socket socket;
	private boolean enabled;

	public BridgeSensorService(long timeoutMillis, int port, String host, boolean enabled)
			throws UnknownHostException, IOException {
		this.enabled = enabled;
		if (enabled) {
			try {
				this.messageQueue = new LinkedBlockingQueue<BridgeSensorMessage>();
				JsonFactory jf = new JsonFactory();
				jf.disable(Feature.AUTO_CLOSE_TARGET);
				this.jsonMapper = new ObjectMapper(jf);
				this.socket = new Socket(host, port);
				this.os = new BufferedOutputStream(socket.getOutputStream());

				String newLine = "\n";
				byte[] newLineBytes = newLine.getBytes();

				Thread thread = new Thread(new Runnable() {

					@Override
					public void run() {
						while (true) {
							try {
								BridgeSensorMessage messageObj = messageQueue.poll(timeoutMillis,
										TimeUnit.MILLISECONDS);
								if (messageObj != null) {
									if (socket.isClosed()) {
										try {
											socket = new Socket(host, port);
											os = new BufferedOutputStream(socket.getOutputStream());
										} catch (Exception e) {
											connectError(e);
											break;
										}
									}

									jsonMapper.writeValue(os, messageObj);
									os.write(newLineBytes);
									os.flush();
								} else {
									os.close();
								}
							} catch (Exception e) {
								logger.error("Error with sending JSON data to bridge sensor");
							}
						}
					}

				}, "Message Thread");
				thread.start();
			} catch (Exception e) {
				connectError(e);
			}
		}
	}

	private void connectError(Exception e) throws IOException {
		logger.error("Error trying to connect to Desktop Bridge Sensor", e);
		BaseAlertMessage alertMessage = new PlainAlertMessage(
				"Error connecting to Desktop Bridge Sensor.  Desktop sensor data will not be collected!",
				"Error connecting to Desktop Bridge Sensor.  Desktop sensor data will not be collected!  Error message: "
						+ e.getClass().toString() + " " + e.getMessage());
		UserAlertingServiceHolder.sendAlert(alertMessage);
	}

	public void sendMessage(BridgeSensorMessage messageObj) {
		if (enabled) {
			messageQueue.add(messageObj);
		}
	}

}
