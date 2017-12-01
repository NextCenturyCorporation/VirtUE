package com.ncc.savior.desktop.virtues;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.xpra.XpraClient;
import com.ncc.savior.desktop.xpra.XpraClient.Status;
import com.ncc.savior.desktop.xpra.XpraConnectionManager;
import com.ncc.savior.desktop.xpra.application.javafx.JavaFxApplicationManagerFactory;
import com.ncc.savior.desktop.xpra.connection.ssh.SshConnectionFactory.SshConnectionParameters;
import com.ncc.savior.desktop.xpra.protocol.keyboard.JavaFxKeyboard;
import com.ncc.savior.desktop.xpra.protocol.keyboard.XpraKeyMap;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.desktop.DesktopVirtue;
import com.ncc.savior.virtueadmin.model.desktop.DesktopVirtueApplication;

/**
 * Interface for backend web service.
 *
 *
 */
public class VirtueService {
	private static final Logger logger = LoggerFactory.getLogger(VirtueService.class);
	private XpraConnectionManager connectionManager;
	private DesktopResourceService desktopResourceService;

	public VirtueService(DesktopResourceService desktopResourceService) {
		// TODO should be dependency injected
		this.desktopResourceService = desktopResourceService;
		JavaFxKeyboard keyboard = new JavaFxKeyboard(new XpraKeyMap());
		this.connectionManager = new XpraConnectionManager(new JavaFxApplicationManagerFactory(keyboard));
	}

	// public void connectAndStartApp(DesktopVirtue app) throws IOException {
	// IConnectionParameters params = app.getConnectionParams();
	// // Do we have an existing client/connection for this params? If so, start a
	// new
	// // app and be done with it.
	//
	// // If we don't have an existing client/connection, then we need to create a
	// // connection and then start the app.
	// XpraClient client = connectionManager.getExistingClient(params);
	// if (client == null || client.getStatus() == Status.ERROR) {
	// client = connectionManager.createClient(params);
	// }
	// connectionManager.startApplication(params, app.getStartCommand());
	// }

	public void ensureConnection(DesktopVirtueApplication app) throws IOException {
		// TODO fix hardcoded
		SshConnectionParameters params = new SshConnectionParameters(app.getHostname(), 22, "user", "password");
		XpraClient client = connectionManager.getExistingClient(params);
		if (client == null || client.getStatus() == Status.ERROR) {
			client = connectionManager.createClient(params);
		}
	}

	public List<DesktopVirtue> getVirtuesForUser() throws IOException {
		List<DesktopVirtue> list = null;
		list = desktopResourceService.getVirtues();
		return list;
	}

	public void startApplication(DesktopVirtue virtue, ApplicationDefinition appDefn) throws IOException {
		// TODO check to see if we have an XPRA connection
		String virtueId = virtue.getId();
		DesktopVirtueApplication app;
		if (virtueId == null) {
			app = desktopResourceService.startApplicationFromTemplate(virtue.getTemplateId(), appDefn);
		} else {
			app = desktopResourceService.startApplication(virtueId, appDefn);
		}
		ensureConnection(app);
	}
}
