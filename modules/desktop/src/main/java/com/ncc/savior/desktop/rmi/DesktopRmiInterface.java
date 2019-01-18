package com.ncc.savior.desktop.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface DesktopRmiInterface extends Remote {

	public void open(String args[]) throws RemoteException;

}
