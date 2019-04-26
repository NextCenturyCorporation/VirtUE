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
package com.ncc.savior.desktop.xpra;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.xpra.connection.BaseConnectListener;
import com.ncc.savior.desktop.xpra.connection.BaseConnectionFactory;
import com.ncc.savior.desktop.xpra.connection.ConnectListenerManager;
import com.ncc.savior.desktop.xpra.connection.IConnectListener;
import com.ncc.savior.desktop.xpra.connection.IConnection;
import com.ncc.savior.desktop.xpra.connection.IConnectionErrorCallback;
import com.ncc.savior.desktop.xpra.connection.IConnectionParameters;
import com.ncc.savior.desktop.xpra.protocol.IPacketHandler;
import com.ncc.savior.desktop.xpra.protocol.IPacketSender;
import com.ncc.savior.desktop.xpra.protocol.InputStreamPacketReader;
import com.ncc.savior.desktop.xpra.protocol.OutputStreamPacketSender;
import com.ncc.savior.desktop.xpra.protocol.encoder.BencodeEncoder;
import com.ncc.savior.desktop.xpra.protocol.encoder.IEncoder;
import com.ncc.savior.desktop.xpra.protocol.keyboard.IKeyboard;
import com.ncc.savior.desktop.xpra.protocol.packet.BasePacketHandler;
import com.ncc.savior.desktop.xpra.protocol.packet.PacketBuilder;
import com.ncc.savior.desktop.xpra.protocol.packet.PacketDistributer;
import com.ncc.savior.desktop.xpra.protocol.packet.PacketListenerManager;
import com.ncc.savior.desktop.xpra.protocol.packet.PacketType;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.HelloPacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.Packet;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.PingEchoPacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.PingPacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.SetDeflatePacket;

/**
 * Client used to interact with Low-Level Xpra protocol. Users of this class can
 * add packet handlers to receive and handle Xpra packets. Much of the automatic
 * internals (like returning pings) is already handled.
 *
 */
public class XpraClient implements Closeable {

	// public static final String VERSION = "0.15.0";
	public static final String VERSION = "2.1.2";
	private static final Logger logger = LoggerFactory.getLogger(XpraClient.class);
	private final IEncoder sendEncoder;
	private PacketDistributer internalPacketDistributer;

	private ConnectListenerManager connectListenerManager;

	private OutputStreamPacketSender packetSender;
	// private IPacketListener packetListener;
	// private IPackatSender packetSender;
	private PacketListenerManager packetReceivedListenerManager;
	private PacketListenerManager packetSentListenerManager;
	// private IConnection connection;
	private IKeyboard keyboard;
	protected InputStreamPacketReader packetReader;
	protected volatile boolean stopReadThread;
	private static int threadCount = 1;
	private Status status;
	private int display;
	protected IConnectionErrorCallback errorCallback;

	public XpraClient() {
		connectListenerManager = new ConnectListenerManager();
		internalPacketDistributer = new PacketDistributer();
		packetReceivedListenerManager = new PacketListenerManager();
		packetSentListenerManager = new PacketListenerManager();
		status = Status.DISCONNECTED;

		internalPacketDistributer.addPacketHandler(PacketType.HELLO, new BasePacketHandler(PacketType.HELLO) {

			@Override
			public void handlePacket(Packet packet) {
				logger.debug("Received Hello Packet=" + packet);
				SetDeflatePacket sendPacket = new SetDeflatePacket(3);
				try {
					packetSender.sendPacket(sendPacket);
				} catch (IOException e) {
					logger.error("Error sending packet=" + sendPacket.toString(), e);
				}
			}
		});

		internalPacketDistributer.addPacketHandler(PacketType.PING, new BasePacketHandler(PacketType.PING) {

			@Override
			public void handlePacket(Packet packet) {
				PingPacket p = (PingPacket) packet;
				PingEchoPacket sendPacket = new PingEchoPacket(p.getTime());
				try {
					packetSender.sendPacket(sendPacket);
					// packetSender.sendPacket(new PingPacket(0));
				} catch (IOException e) {
					onIoException(e);
				}
			}
		});
		sendEncoder = new BencodeEncoder();
		BaseConnectListener listener = new BaseConnectListener() {

			@Override
			public void onConnectSuccess(IConnection connection) {
				callOnSuccess(connection);
			}

		};
		connectListenerManager.addListener(listener);
	}

	public void connect(BaseConnectionFactory factory, IConnectionParameters params) {
		// this.factory = factory;
		// this.params = params;
		factory.connect(params, connectListenerManager);
	}

	public void callOnSuccess(IConnection connection) {
		logger.trace("success on connection = " + connection + " client=" + this);
		status = Status.CONNECTED;

		IConnectionErrorCallback myErrorCallback = new IConnectionErrorCallback() {
			@Override
			public void onError(String description, IOException e) {
				onIoException(e);
				if (errorCallback != null) {
					errorCallback.onError(description, e);
				}
			}
		};
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				try {
					InputStream in = connection.getInputStream();
					packetReader = new InputStreamPacketReader(in, new PacketBuilder());
					Packet packet = null;
					while (!stopReadThread && (packet = packetReader.getNextPacket()) != null) {
						internalPacketDistributer.handlePacket(packet);
						packetReceivedListenerManager.handlePacket(packet);
					}

				} catch (IOException e) {
					logger.warn("problem reading from connection " + connection.getConnectionParameters() + ": " + e);
					onIoException(e);
				}
			}
		};
		Thread thread = new Thread(runnable, "PacketReader-" + threadCount++);
		thread.setDaemon(true);

		HelloPacket helloPacket = HelloPacket.createDefaultRequest();
		helloPacket.setKeyMap(keyboard.getKeyMap());
		try {
			packetSender = new OutputStreamPacketSender(connection.getOutputStream(), sendEncoder, myErrorCallback);
			packetSender.setPacketListenerManager(packetSentListenerManager);
			packetSender.sendPacket(helloPacket);
			logger.debug("Sent hello packet=" + helloPacket);
			// packetSender.sendPacket(new SetDeflatePacket(3));
			thread.start();
		} catch (IOException e) {
			logger.warn("could not send hello packet on connection " + connection.getConnectionParameters() + ": " + e);
			onIoException(e);
		}
	}

	public IPacketSender getPacketSender() {
		return packetSender;
	}

	private void onIoException(IOException e) {
		logger.error("Error with Xpra connection", e);
		close();
		// TODO consider some type of retry
		// connect(factory, params);
	}

	public void addConnectListener(IConnectListener listener) {
		connectListenerManager.addListener(listener);
	}

	public void removeConnectListener(IConnectListener listener) {
		connectListenerManager.removeListener(listener);
	}

	public void addPacketListener(IPacketHandler handler) {
		packetReceivedListenerManager.addPacketHandler(handler);
	}

	public void removePacketListener(IPacketHandler handler) {
		packetReceivedListenerManager.removePacketHandler(handler);
	}

	public void addPacketSendListener(IPacketHandler handler) {
		packetSentListenerManager.addPacketHandler(handler);
	}

	public void removePacketSendListener(IPacketHandler handler) {
		packetSentListenerManager.removePacketHandler(handler);
	}

	public IKeyboard getKeyboard() {
		return keyboard;
	}

	public Status getStatus() {
		return status;
	}

	public void setKeyboard(IKeyboard keyboard) {
		this.keyboard = keyboard;
	}

	public static enum Status {
		DISCONNECTED, CONNECTED, ERROR
	}

	@Override
	public void close() {
		this.status = XpraClient.Status.ERROR;
		if (packetSender != null) {
			try {
				packetSender.close();
			} catch (IOException e1) {
				logger.error("Error closing packetSender");
			}
		}
		if (packetReader != null) {
			try {
				packetReader.close();
			} catch (IOException e1) {
				logger.error("Error closing packetReader");
			}
		}
		stopReadThread = true;
	}

	public int getDisplay() {
		return display;
	}

	public void setDisplay(int display) {
		this.display = display;
	}

	public void setErrorCallback(IConnectionErrorCallback errorCallback) {
		this.errorCallback = errorCallback;
	}
}
