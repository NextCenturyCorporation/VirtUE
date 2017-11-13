package com.ncc.savior.desktop.virtues;

import java.util.ArrayList;
import java.util.List;

import com.ncc.savior.desktop.xpra.XpraClient;
import com.ncc.savior.desktop.xpra.XpraClient.Status;
import com.ncc.savior.desktop.xpra.XpraConnectionManager;
import com.ncc.savior.desktop.xpra.connection.IConnectionParameters;
import com.ncc.savior.desktop.xpra.connection.ssh.SshConnectionFactory;
import com.ncc.savior.desktop.xpra.connection.ssh.SshConnectionFactory.SshConnectionParameters;
import com.ncc.savior.desktop.xpra.connection.tcp.TcpConnectionFactory;

public class VirtueService {
	private XpraConnectionManager connectionManager;

	public VirtueService() {
		// TODO should be dependency injected
		this.connectionManager = new XpraConnectionManager();
	}

	public void connectAndStartApp(VirtueAppDto app) {
		IConnectionParameters params = app.getConnectionParams();
		// Do we have an existing client/connection for this params? If so, start a new
		// app and be done with it.

		// If we don't have an existing client/connection, then we need to create a
		// connection and then start the app.
		XpraClient client = connectionManager.getExistingClient(params);
		if (client == null || client.getStatus() == Status.ERROR) {
			client = connectionManager.createClient(params);
		}
		connectionManager.startApplication(params, app.getStartCommand());
	}

	public List<VirtueDto> getVirtuesForUser() {
		ArrayList<VirtueDto> virtue = new ArrayList<VirtueDto>();
		virtue.add(new VirtueDto("Web Browsers (TCP)",
				new VirtueAppDto("Chrome", "", "",
						new TcpConnectionFactory.TcpConnectionParameters("localhost", 10000)),
				new VirtueAppDto("Firefox", "", "",
						new TcpConnectionFactory.TcpConnectionParameters("localhost", 10001))));

		SshConnectionParameters sshConParam1 = new SshConnectionFactory.SshConnectionParameters("localhost", 22, "user",
				"password");

		virtue.add(new VirtueDto("Web Browsers (SSH)", new VirtueAppDto("Chrome", "", "google-chrome", sshConParam1),
				new VirtueAppDto("Firefox", "", "firefox", sshConParam1)));

		virtue.add(new VirtueDto("Other (SSH)", new VirtueAppDto("GEdit", "", "gedit", sshConParam1),
				new VirtueAppDto("Calculator", "", "gnome-calculator", sshConParam1),
				new VirtueAppDto("Terminal", "", "gnome-terminal", sshConParam1)));

		// virtue.add(new VirtueDto("Microsoft Office", new VirtueAppDto("Word", ""),
		// new VirtueAppDto("Excel", ""),
		// new VirtueAppDto("Powerpoint", "")));
		//
		// virtue.add(new VirtueDto("Drawing", new VirtueAppDto("Paint", ""), new
		// VirtueAppDto("Gimp", "")));
		//
		// virtue.add(new VirtueDto("Web Browsers2", new VirtueAppDto("Chrome", ""), new
		// VirtueAppDto("Firefox", ""),
		// new VirtueAppDto("Netscape", "")));
		//
		// virtue.add(new VirtueDto("Web Browsers3", new VirtueAppDto("Chrome", ""), new
		// VirtueAppDto("Firefox", ""),
		// new VirtueAppDto("Netscape", "")));
		//
		// virtue.add(new VirtueDto("Web Browsers4", new VirtueAppDto("Chrome", ""), new
		// VirtueAppDto("Firefox", ""),
		// new VirtueAppDto("Netscape", "")));

		return virtue;

	}

}
