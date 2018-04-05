package com.ncc.savior.desktop.virtues;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.ws.rs.client.ResponseProcessingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.rdp.RdpConnectionManager;
import com.ncc.savior.desktop.sidebar.RgbColor;
import com.ncc.savior.desktop.xpra.IApplicationManagerFactory;
import com.ncc.savior.desktop.xpra.XpraClient;
import com.ncc.savior.desktop.xpra.XpraClient.Status;
import com.ncc.savior.desktop.xpra.XpraConnectionManager;
import com.ncc.savior.network.SshConnectionParameters;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.OS;
import com.ncc.savior.virtueadmin.model.VirtualMachineTemplate;
import com.ncc.savior.virtueadmin.model.desktop.BaseApplicationInstance;
import com.ncc.savior.virtueadmin.model.desktop.DesktopVirtue;

/**
 * Interface for backend web service.
 *
 *
 */
public class VirtueService {
	private static final Logger logger = LoggerFactory.getLogger(VirtueService.class);
	private XpraConnectionManager xpraConnectionManager;
	private DesktopResourceService desktopResourceService;
	private RdpConnectionManager rdpConnectionManager;

	public VirtueService(DesktopResourceService desktopResourceService, IApplicationManagerFactory appManger) {
		this.desktopResourceService = desktopResourceService;
		this.xpraConnectionManager = new XpraConnectionManager(appManger);
		this.rdpConnectionManager = new RdpConnectionManager();
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

	protected void ensureConnection(BaseApplicationInstance app, DesktopVirtue virtue, RgbColor color)
			throws IOException {
		File file = null;
		try {
			String key = app.getPrivateKey();

			SshConnectionParameters params = null;
			if (key != null && key.contains("BEGIN RSA PRIVATE KEY")) {
				params = createSshParamsWithKeyFile(app);
				file = params.getPem();
			} else {
				params = new SshConnectionParameters(app.getHostname(), app.getPort(), app.getUserName(), key);
			}
			String colorDesc = (color == null ? "" : " with color " + color.toString());
			logger.debug("verifying connection to " + app.getHostname() + colorDesc);
			XpraClient client = xpraConnectionManager.getExistingClient(params);
			if (client == null || client.getStatus() == Status.ERROR) {
				logger.debug("needed new connection");
				client = xpraConnectionManager.createClient(params, color);
			}
		} finally {
			if (file != null && file.exists()) {
				file.delete();
			}
		}
	}

	protected SshConnectionParameters createSshParamsWithKeyFile(BaseApplicationInstance app)
			throws IOException {
		File pem = Files.createTempFile(app.getApplicationDefinition().getName(), ".pem").toFile();
		FileWriter writer = new FileWriter(pem);
		writer.write(app.getPrivateKey());
		writer.close();
		SshConnectionParameters params = new SshConnectionParameters(app.getHostname(), app.getPort(),
				app.getUserName(), pem);
		return params;
	}

	public List<DesktopVirtue> getVirtuesForUser() throws IOException {
		List<DesktopVirtue> list = null;
		list = desktopResourceService.getVirtues();
		// addDevelopmentVirtues(list);
		return list;
	}

	/**
	 * allow warning only once per run
	 */
	static private boolean addDevelopmentVirtuesInvoked = false;

	/**
	 * Add Virtues for use in development & testing only
	 *
	 * @param list
	 */
	@SuppressWarnings("unused")
	private void addDevelopmentVirtues(List<DesktopVirtue> list) {
		if (!addDevelopmentVirtuesInvoked) {
			logger.warn("development and testing virtues enabled");
			addDevelopmentVirtuesInvoked = true;
		}
		String windowsTemplatePath = "dummy windows template path";
		ApplicationDefinition edge = new ApplicationDefinition("Edge", "1.0", OS.WINDOWS, "edge.exe");
		Set<ApplicationDefinition> windowsApps = Collections.singleton(edge);
		VirtualMachineTemplate windowsVMTemplate = new VirtualMachineTemplate("Windows Test VM", OS.WINDOWS,
				windowsTemplatePath, windowsApps, true, new Date(), "system");
		DesktopVirtue windowsVirtue = new DesktopVirtue(null, "Windows Test", windowsVMTemplate.getId(), windowsApps);
		list.add(windowsVirtue);
	}

	public void startApplication(DesktopVirtue virtue, ApplicationDefinition appDefn, RgbColor color)
			throws IOException, ResponseProcessingException {
		// TODO check to see if we have an XPRA connection
		String virtueId = virtue.getId();
		BaseApplicationInstance app;
		if (virtueId == null) {
			app = desktopResourceService.startApplicationFromTemplate(virtue.getTemplateId(), appDefn);
		} else {
			app = desktopResourceService.startApplication(virtueId, appDefn);
		}
		switch (appDefn.getOs()) {
		case WINDOWS:
			// Since we use RDP for Windows clients, we have to start the app at the same
			// time as the RDP session
			rdpConnectionManager.startApplication(virtue, app, color, createSshParamsWithKeyFile(app));
			break;
		case LINUX:
			ensureConnection(app, virtue, color);
			break;
		case MAC:
			throw new UnsupportedOperationException("cannot start apps on remote machines running Mac OS X");
		default:
			throw new UnsupportedOperationException("unknown OS: " + appDefn.getOs());
		}
	}
}
