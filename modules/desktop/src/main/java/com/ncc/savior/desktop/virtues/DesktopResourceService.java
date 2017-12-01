package com.ncc.savior.desktop.virtues;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.desktop.DesktopVirtue;
import com.ncc.savior.virtueadmin.model.desktop.DesktopVirtueApplication;

public class DesktopResourceService {
	private Client client;
	private ObjectMapper jsonMapper;
	private WebTarget baseApi;

	public DesktopResourceService(String baseApiUri) {
		// TODO should be dependency injected
		client = ClientBuilder.newClient();
		jsonMapper = new ObjectMapper();
		baseApi = client.target(baseApiUri);
	}

	public List<DesktopVirtue> getVirtues() throws IOException {
		InputStream in = getListOfClass("virtue", "GET");
		List<DesktopVirtue> instances = jsonMapper.readValue(in, new TypeReference<List<DesktopVirtue>>() {
		});
		return instances;
	}

	private InputStream getListOfClass(String path, String method) throws IOException {
		WebTarget target = baseApi.path(path);
		Response response = target.request(MediaType.APPLICATION_JSON_TYPE).method(method);
		InputStream in = (InputStream) response.getEntity();
		return in;
	}

	private <T> T getClass(WebTarget target, String method, Class<T> klass) throws IOException {
		Response response = target.request(MediaType.APPLICATION_JSON_TYPE).method(method);
		InputStream in = (InputStream) response.getEntity();
		T instance = jsonMapper.readValue(in, klass);
		return instance;
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
}
