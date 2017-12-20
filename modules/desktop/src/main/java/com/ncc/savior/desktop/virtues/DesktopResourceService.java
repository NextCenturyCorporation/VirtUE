package com.ncc.savior.desktop.virtues;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ncc.savior.desktop.authorization.AuthorizationService;
import com.ncc.savior.desktop.authorization.DesktopUser;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.desktop.DesktopVirtue;
import com.ncc.savior.virtueadmin.model.desktop.DesktopVirtueApplication;

public class DesktopResourceService {
	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(DesktopResourceService.class);
	private Client client;
	private ObjectMapper jsonMapper;
	private WebTarget baseApi;
	private AuthorizationService authService;

	public DesktopResourceService(AuthorizationService authService, String baseApiUri) {
		this.authService = authService;
		if (true) {
			client = ClientBuilder.newBuilder().hostnameVerifier(new HostnameVerifier() {
				@Override
				public boolean verify(String hostname, SSLSession session) {
					return true;
				}
			}).build();
		} else {
			client = ClientBuilder.newClient();
		}
		jsonMapper = new ObjectMapper();
		baseApi = client.target(baseApiUri);
	}

	public List<DesktopVirtue> getVirtues() throws IOException {
		List<DesktopVirtue> instances;
		try {
			InputStream in = getListOfClass("virtue", "GET");
			instances = jsonMapper.readValue(in, new TypeReference<List<DesktopVirtue>>() {
			});
		} catch (IOException | ProcessingException e) {
			logger.error("error attmepting to get virtues", e);
			instances = new ArrayList<DesktopVirtue>();
		}
		return instances;
	}

	public DesktopVirtueApplication startApplication(String virtueId, ApplicationDefinition appDefn)
			throws IOException {
		WebTarget target = baseApi.path("virtue").path(virtueId).path(appDefn.getId()).path("start");
		DesktopVirtueApplication returnedApp = getClass(target, "GET", DesktopVirtueApplication.class);
		return returnedApp;

	}

	public DesktopVirtueApplication startApplicationFromTemplate(String templateId, ApplicationDefinition appDefn)
			throws IOException {
		WebTarget target = baseApi.path("template").path(templateId).path(appDefn.getId()).path("start");
		DesktopVirtueApplication returnedApp = getClass(target, "GET", DesktopVirtueApplication.class);
		return returnedApp;
	}

	private InputStream getListOfClass(String path, String method) throws IOException {
		WebTarget target = baseApi.path(path);
		Builder builder = target.request(MediaType.APPLICATION_JSON_TYPE);
		addAuthorization(builder);
		Response response = builder.method(method);

		InputStream in = (InputStream) response.getEntity();
		return in;
	}

	private <T> T getClass(WebTarget target, String method, Class<T> klass) throws IOException {
		Builder builder = target.request(MediaType.APPLICATION_JSON_TYPE);
		addAuthorization(builder);
		Response response = builder.method(method);
		if (response.getStatus() == 200) {
		InputStream in = (InputStream) response.getEntity();
		T instance = jsonMapper.readValue(in, klass);
		return instance;
		} else {
			throw new RuntimeException("FIX ME!!!!!" + response.getStatus() + " : " + response.getEntity().toString());
		}
	}

	private void addAuthorization(Builder builder) {
		// Temporary implementation until we really tie in active directory.
		DesktopUser user = authService.getUser();
		if (user != null) {
			String username = user.getUsername();
			builder.header("X-Authorization", username);
		}
	}
}
