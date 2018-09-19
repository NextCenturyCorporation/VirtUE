package com.ncc.savior.desktop.clipboard.defaultApplications;
import java.rmi.Remote;
import java.rmi.RemoteException;

import com.ncc.savior.desktop.clipboard.messages.DefaultApplicationMessage.DefaultApplicationType;

public interface RmiInterface extends Remote {

	public void open(DefaultApplicationType app, String args[]) throws RemoteException;

}
