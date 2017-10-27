package com.ncc.savior.desktop.xpra;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.xpra.connection.BaseConnectListener;
import com.ncc.savior.desktop.xpra.connection.BaseConnectionFactory;
import com.ncc.savior.desktop.xpra.connection.ConnectListenerManager;
import com.ncc.savior.desktop.xpra.connection.IConnectListener;
import com.ncc.savior.desktop.xpra.connection.IConnection;
import com.ncc.savior.desktop.xpra.connection.IConnectionParameters;
import com.ncc.savior.desktop.xpra.protocol.IPacketHandler;
import com.ncc.savior.desktop.xpra.protocol.IPacketSender;
import com.ncc.savior.desktop.xpra.protocol.InputStreamPacketReader;
import com.ncc.savior.desktop.xpra.protocol.OutputStreamPacketSender;
import com.ncc.savior.desktop.xpra.protocol.encoder.BencodeEncoder;
import com.ncc.savior.desktop.xpra.protocol.encoder.IEncoder;
import com.ncc.savior.desktop.xpra.protocol.keyboard.IKeyMap;
import com.ncc.savior.desktop.xpra.protocol.packet.BasePacketHandler;
import com.ncc.savior.desktop.xpra.protocol.packet.PacketBuilder;
import com.ncc.savior.desktop.xpra.protocol.packet.PacketDistributer;
import com.ncc.savior.desktop.xpra.protocol.packet.PacketListenerManager;
import com.ncc.savior.desktop.xpra.protocol.packet.PacketType;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.ConfigureWindowPacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.HelloPacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.NewWindowPacket;
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
public class XpraClient {

	// public static final String VERSION = "0.15.0";
	public static final String VERSION = "2.1.2";
	private static final Logger logger = LoggerFactory.getLogger(XpraClient.class);
	private final IEncoder sendEncoder;
	private PacketDistributer internalPacketDistributer;

	private ConnectListenerManager connectListenerManager = new ConnectListenerManager();

	private OutputStreamPacketSender packetSender;
	// private IPacketListener packetListener;
	// private IPackatSender packetSender;
	private PacketListenerManager packetReceivedListenerManager;
	private PacketListenerManager packetSentListenerManager;
	private IConnection connection;
	private IKeyMap keyMap;

	public XpraClient() {
		internalPacketDistributer = new PacketDistributer();
		packetReceivedListenerManager = new PacketListenerManager();
		packetSentListenerManager = new PacketListenerManager();

		internalPacketDistributer.addPacketHandler(PacketType.HELLO, new BasePacketHandler(PacketType.HELLO) {

			@Override
			public void handlePacket(Packet packet) {
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

		internalPacketDistributer.addPacketHandler(PacketType.NEW_WINDOW, new BasePacketHandler(PacketType.NEW_WINDOW) {

			@Override
			public void handlePacket(Packet packet) {
				NewWindowPacket p = (NewWindowPacket) packet;
				ConfigureWindowPacket sendPacket = new ConfigureWindowPacket(p.getWindowId(), p.getX(), p.getY(),
						p.getWidth(), p.getHeight());
				try {
					packetSender.sendPacket(sendPacket);
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
		// Make sure that factory calls the listeners registered to the client
		factory.addListener(new IConnectListener() {
			@Override
			public void onConnectSuccess(IConnection connection) {
				connectListenerManager.onConnectionSuccess(connection);
			}

			@Override
			public void onConnectFailure(IConnectionParameters params, IOException e) {
				connectListenerManager.onConnectionFailure(params, e);
			}

			@Override
			public void onBeforeConnectAttempt(IConnectionParameters parameters) {
				connectListenerManager.onBeforeConnectionAttempt(parameters);
			}
		});
		connection = factory.connect(params);
	}

	public void callOnSuccess(IConnection connection) {
		try {
			packetSender = new OutputStreamPacketSender(connection.getOutputStream(), sendEncoder);
			packetSender.setPacketListenerManager(packetSentListenerManager);
			Runnable runnable = new Runnable() {

				@Override
				public void run() {
					try {
						InputStreamPacketReader packetReader = new InputStreamPacketReader(connection.getInputStream(),
								new PacketBuilder());
						Packet packet = null;
						while ((packet = packetReader.getNextPacket()) != null) {
							internalPacketDistributer.handlePacket(packet);
							packetReceivedListenerManager.handlePacket(packet);
						}

					} catch (IOException e) {
						onIoException(e);
					}
				}
			};
			Thread thread = new Thread(runnable);

			HelloPacket helloPacket = HelloPacket.createDefaultRequest();
			helloPacket.setKeyMap(keyMap);
			packetSender.sendPacket(helloPacket);
			// packetSender.sendPacket(new SetDeflatePacket(3));
			thread.start();
		} catch (IOException e) {
			onIoException(e);
		}
	}

	// TODO do we need both sendPAcket method and getPacketSEnder()? Which is
	// better?
	public void sendPacket(Packet packet) throws IOException {
		this.packetSender.sendPacket(packet);
	}

	public IPacketSender getPacketSender() {
		return packetSender;
	}

	private void onIoException(IOException e) {
		logger.error("Found IOException for connection=" + connection, e);
		// TODO figure out how this should work
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

	public IKeyMap getKeyMap() {
		return keyMap;
	}

	public void setKeyMap(IKeyMap keyMap) {
		this.keyMap = keyMap;
	}
}
