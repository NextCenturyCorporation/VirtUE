package com.ncc.savior.desktop.virtues;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ncc.savior.desktop.xpra.XpraClient;
import com.ncc.savior.desktop.xpra.XpraClient.Status;
import com.ncc.savior.desktop.xpra.XpraConnectionManager;
import com.ncc.savior.desktop.xpra.application.javafx.JavaFxApplicationManagerFactory;
import com.ncc.savior.desktop.xpra.connection.IConnectionParameters;
import com.ncc.savior.desktop.xpra.protocol.keyboard.JavaFxKeyboard;
import com.ncc.savior.desktop.xpra.protocol.keyboard.XpraKeyMap;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.VirtueInstance;
import com.ncc.savior.virtueadmin.model.VirtueTemplate;

/**
 * Interface for backend web service.
 *
 *
 */
public class VirtueService {
	private static final Logger logger = LoggerFactory.getLogger(VirtueService.class);
	private XpraConnectionManager connectionManager;
	private Client client;
	private WebTarget baseApi;
	private ObjectMapper jsonMapper;

	public VirtueService(String baseApiUri) {
		// TODO should be dependency injected
		client = ClientBuilder.newClient();
		jsonMapper = new ObjectMapper();
		baseApi = client.target(baseApiUri);
		JavaFxKeyboard keyboard = new JavaFxKeyboard(new XpraKeyMap());
		this.connectionManager = new XpraConnectionManager(new JavaFxApplicationManagerFactory(keyboard));
	}

	public void connectAndStartApp(VirtueAppDto app) throws IOException {
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

	private List<VirtueTemplate> getVirtueTemplates() throws IOException {
		WebTarget path = baseApi.path("user/virtue/template");
		Response response = path.request(MediaType.APPLICATION_JSON_TYPE).get();
		InputStream in = (InputStream) response.getEntity();
		List<VirtueTemplate> templates = jsonMapper.readValue(in, new TypeReference<List<VirtueTemplate>>() {
		});
		return templates;
	}

	private List<VirtueInstance> getVirtues() throws IOException {
		WebTarget path = baseApi.path("user/virtue/");
		Response response = path.request(MediaType.APPLICATION_JSON_TYPE).get();
		InputStream in = (InputStream) response.getEntity();
		List<VirtueInstance> instances = jsonMapper.readValue(in, new TypeReference<List<VirtueInstance>>() {
		});
		return instances;
	}

	public List<VirtueDto> getVirtuesForUser() {
		List<VirtueInstance> templates;
		ArrayList<VirtueDto> list = new ArrayList<VirtueDto>();

		try {
			templates = getVirtues();
			Iterator<VirtueInstance> itr = templates.iterator();
			while (itr.hasNext()) {
				VirtueInstance instance = itr.next();

				Set<ApplicationDefinition> appDefs = instance.getApplications();

				List<VirtueAppDto> apps = new ArrayList<VirtueAppDto>();
				for (ApplicationDefinition app : appDefs) {
					// String id = app.getId();
					String name = app.getName();
					String startCommand = app.getLaunchCommand();
					IConnectionParameters conParams = null;
					apps.add(new VirtueAppDto(name, (String) "", startCommand, conParams));
				}
				VirtueDto virtue = new VirtueDto(instance.getId(), instance.getName(), instance.getTemplateId(), apps);
				virtue.setStatus(instance.getState());
				list.add(virtue);
			}
			return list;
		} catch (IOException | ProcessingException e) {
			logger.error("Error getting virtues", e);
			return new ArrayList<VirtueDto>();
		}
		// ArrayList<VirtueDto> virtue = new ArrayList<VirtueDto>();
		// virtue.add(new VirtueDto("Web Browsers (TCP)",
		// new VirtueAppDto("Chrome", "", "",
		// new TcpConnectionFactory.TcpConnectionParameters("localhost", 10000)),
		// new VirtueAppDto("Firefox", "", "",
		// new TcpConnectionFactory.TcpConnectionParameters("localhost", 10001))));
		//
		// SshConnectionParameters sshConParam1 = new
		// SshConnectionFactory.SshConnectionParameters("localhost", 22, "user",
		// "password");
		// SshConnectionParameters sshBadConParam = new
		// SshConnectionFactory.SshConnectionParameters("badhost", 8000,
		// "user",
		// "password");
		//
		// virtue.add(new VirtueDto("Web Browsers (SSH)", new VirtueAppDto("Chrome", "",
		// "google-chrome", sshConParam1),
		// new VirtueAppDto("Firefox", "", "firefox", sshConParam1)));
		//
		// virtue.add(new VirtueDto("Other (SSH)", new VirtueAppDto("GEdit", "",
		// "gedit", sshConParam1),
		// new VirtueAppDto("Calculator", "", "gnome-calculator", sshConParam1),
		// new VirtueAppDto("Terminal", "", "gnome-terminal", sshConParam1),
		// new VirtueAppDto("Error App", "", "error", sshBadConParam)));
		//
		// return virtue;

	}

	public void provisionVirtue(VirtueDto virtue) {
		WebTarget path = baseApi.path("user/virtue/template").path(virtue.getTemplateId()).path("create");
		Response response = path.request(MediaType.APPLICATION_JSON_TYPE).get();
		InputStream in = (InputStream) response.getEntity();
		try {
			VirtueInstance instance = jsonMapper.readValue(in, VirtueInstance.class);
			logger.debug("Provisioned virtue.  Instance=" + instance);
		} catch (IOException e) {
			logger.error("Error attempting provision virtue=" + virtue, e);
		}
	}

	public void startVirtue(VirtueDto virtue) {
		WebTarget path = baseApi.path("user/virtue").path(virtue.getId()).path("launch");
		Response response = path.request(MediaType.APPLICATION_JSON_TYPE).get();
		InputStream in = (InputStream) response.getEntity();
		try {
			VirtueInstance instance = jsonMapper.readValue(in, VirtueInstance.class);
			logger.debug("Started virtue.  Instance=" + instance);
		} catch (IOException e) {
			logger.error("Error attempting start virtue=" + virtue, e);
		}
	}

}
