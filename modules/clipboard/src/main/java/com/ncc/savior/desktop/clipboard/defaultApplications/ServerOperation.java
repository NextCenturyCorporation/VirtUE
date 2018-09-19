package com.ncc.savior.desktop.clipboard.defaultApplications;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import com.ncc.savior.desktop.clipboard.messages.DefaultApplicationMessage.DefaultApplicationType;

public class ServerOperation extends UnicastRemoteObject implements RmiInterface {

	private static final long serialVersionUID = 1L;

	protected ServerOperation() throws RemoteException {

		super();

	}


	public static void main(String[] args) {

		try {
			// Registry registry = LocateRegistry.getRegistry();
			// registry.bind("//127.0.0.1/MyServer", new ServerOperation());
			Naming.rebind("//127.0.0.1/MyServer", new ServerOperation());
			System.err.println("Server ready");

		} catch (Exception e) {

			System.err.println("Server exception: " + e.toString());
			e.printStackTrace();

		}

	}

	@Override
	public void open(DefaultApplicationType app, String[] args) throws RemoteException {
		// TODO Auto-generated method stub

	}

}