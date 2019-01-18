package com.ncc.savior.desktop.rmi;

import java.io.IOException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.clipboard.IClipboardMessageSenderReceiver;
import com.ncc.savior.desktop.clipboard.defaultApplications.RmiInterface;
import com.ncc.savior.desktop.clipboard.defaultApplications.RmiServer;
import com.ncc.savior.desktop.clipboard.messages.DefaultApplicationMessage;

public class DesktopRmiServer extends UnicastRemoteObject implements RmiInterface {
	private static final String SERVICE_NAME = "SaviorDesktop";
	public static final String RMI_NAME = "//localhost/" + SERVICE_NAME;
	private static final Logger logger = LoggerFactory.getLogger(RmiServer.class);
	private static final long serialVersionUID = 1L;
	// registry needed to prevent it from being garbage collected.
	@SuppressWarnings("unused")
	private static Registry registry;
	private IClipboardMessageSenderReceiver transmitter;
	private String sourceId;

	protected DesktopRmiServer(String sourceId, IClipboardMessageSenderReceiver transmitter) throws RemoteException {
		super();
		this.transmitter = transmitter;
		this.sourceId = sourceId;
	}

	public static void bindServer(String sourceId, IClipboardMessageSenderReceiver transmitter) {
		Runnable r = () -> {
			try {
				registry = LocateRegistry.createRegistry(1099);
				Naming.rebind(RMI_NAME, new DesktopRmiServer(sourceId, transmitter));
				logger.info("RMI Server ready");
			} catch (Exception e) {
				logger.error("RMI Server exception ", e);
			}
		};
		// Thread t = new Thread(r, "RMI-Server");
		// t.setDaemon(true);
		// t.start();
		r.run();
	}

	@Override
	public void open(String[] args) throws RemoteException {
		DefaultApplicationMessage msg = new DefaultApplicationMessage(sourceId, app, args);
		try {
			transmitter.sendMessageToHub(msg);
		} catch (IOException e) {
			logger.error("Error sending DefaultApplicationMessage " + msg);
		}
	}

	public static void main(String[] args) {
		bindServer(null, null);
	}
}