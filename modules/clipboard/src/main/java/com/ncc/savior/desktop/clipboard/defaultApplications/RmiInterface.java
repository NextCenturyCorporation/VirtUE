package com.ncc.savior.desktop.clipboard.defaultApplications;

import java.rmi.Remote;
import java.rmi.RemoteException;

import com.ncc.savior.desktop.clipboard.messages.DefaultApplicationMessage.DefaultApplicationType;

/**
 * Interface that RMI server must implement for opening a default app.
 */
public interface RmiInterface extends Remote {

	/**
	 * Calling this should lead to an application opening the type of application
	 * specified with the arguments.
	 * 
	 * @param app
	 * @param args
	 * @throws RemoteException
	 */
	public void open(DefaultApplicationType app, String args[]) throws RemoteException;

}
