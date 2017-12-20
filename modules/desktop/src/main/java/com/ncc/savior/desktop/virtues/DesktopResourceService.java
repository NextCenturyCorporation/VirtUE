package com.ncc.savior.desktop.virtues;

import java.io.IOException;
import java.io.InputStream;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
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

	public DesktopResourceService(AuthorizationService authService, String baseApiUri, boolean allowAllHostnames) {
		this.authService = authService;
		if (allowAllHostnames) {
			try {
				client = getIgnoreSSLClient();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		} else {
			client = ClientBuilder.newClient();
		}
		jsonMapper = new ObjectMapper();
		baseApi = client.target(baseApiUri);
	}

	public DesktopResourceService(AuthorizationService authService, String baseApiUri) {
		this(authService, baseApiUri, false);
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

	public static Client getIgnoreSSLClient() throws Exception {
		SSLContext sslcontext = SSLContext.getInstance("TLS");
		sslcontext.init(null, new TrustManager[] { new X509TrustManager() {

			@Override
			public X509Certificate[] getAcceptedIssuers() {
				return new X509Certificate[0];
			}

			@Override
			public void checkClientTrusted(java.security.cert.X509Certificate[] arg0, String arg1)
					throws java.security.cert.CertificateException {

			}

			@Override
			public void checkServerTrusted(java.security.cert.X509Certificate[] arg0, String arg1)
					throws java.security.cert.CertificateException {

			}

		} }, new java.security.SecureRandom());
		return ClientBuilder.newBuilder().sslContext(sslcontext).hostnameVerifier((s1, s2) -> true).build();
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
