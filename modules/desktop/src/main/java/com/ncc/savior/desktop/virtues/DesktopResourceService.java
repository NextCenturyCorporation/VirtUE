package com.ncc.savior.desktop.virtues;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidParameterException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
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
import javax.ws.rs.client.ResponseProcessingException;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ncc.savior.desktop.authorization.AuthorizationService;
import com.ncc.savior.desktop.authorization.DesktopUser;
import com.ncc.savior.desktop.authorization.InvalidUserLoginException;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.desktop.BaseApplicationInstance;
import com.ncc.savior.virtueadmin.model.desktop.DesktopVirtue;

public class DesktopResourceService {
	private static final Logger logger = LoggerFactory.getLogger(DesktopResourceService.class);
	private Client client;
	private ObjectMapper jsonMapper;
	private WebTarget baseApi;
	private AuthorizationService authService;
	private String targetHost;

	public DesktopResourceService(AuthorizationService authService, String baseApiUri, boolean allowAllHostnames)
			throws InvalidParameterException, KeyManagementException, NoSuchAlgorithmException {
		try {
			this.targetHost = new URI(baseApiUri).getHost();
		} catch (URISyntaxException e) {
			String error = "Unable to get Subject Principal Name from baseUrl=" + baseApiUri;
			logger.error(error, e);
			InvalidParameterException exception = new InvalidParameterException(error);
			exception.initCause(e);
			throw exception;
		}
		this.authService = authService;
		if (allowAllHostnames) {
			client = getIgnoreSSLClient();
		} else {
			client = ClientBuilder.newClient();
		}
		jsonMapper = new ObjectMapper();
		baseApi = client.target(baseApiUri);
	}

	public List<DesktopVirtue> getVirtues() throws IOException {
		List<DesktopVirtue> instances;
		try {
			Response r = getListOfClass("virtue", "GET");
			InputStream in = (InputStream) r.getEntity();
			if (r.getStatus() >= 400) {
				String data = streamToString(in);
				logger.error("response (" + r.getStatus() + "): " + data);
				instances = new ArrayList<DesktopVirtue>();
			} else {
				instances = jsonMapper.readValue(in, new TypeReference<List<DesktopVirtue>>() {
				});
			}
		} catch (IOException | ProcessingException e) {

			logger.error("error attmepting to get virtues.", e);
			instances = new ArrayList<DesktopVirtue>();
		}
		return instances;
	}

	private String streamToString(InputStream bin) {
		StringBuilder result = new StringBuilder();
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(bin));
			String line;
			boolean flag = false;
			String newLine = System.getProperty("line.separator");
			while ((line = reader.readLine()) != null) {
				result.append(flag ? newLine : "").append(line);
				flag = true;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result.toString();
	}

	public BaseApplicationInstance startApplication(String virtueId, ApplicationDefinition appDefn)
			throws IOException, ResponseProcessingException {
		WebTarget target = baseApi.path("virtue").path(virtueId).path(appDefn.getId()).path("start");
		BaseApplicationInstance returnedApp = getClass(target, "GET", BaseApplicationInstance.class);
		logger.debug("Started app=" + returnedApp);
		return returnedApp;

	}

	public BaseApplicationInstance startApplicationFromTemplate(String templateId, ApplicationDefinition appDefn)
			throws IOException, ResponseProcessingException {
		WebTarget target = baseApi.path("template").path(templateId).path(appDefn.getId()).path("start");
		BaseApplicationInstance returnedApp = getClass(target, "GET", BaseApplicationInstance.class);
		logger.debug("Started app=" + returnedApp);
		return returnedApp;
	}

	public static Client getIgnoreSSLClient() throws NoSuchAlgorithmException, KeyManagementException {
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

	private Response getListOfClass(String path, String method) throws IOException, InvalidUserLoginException {
		WebTarget target = baseApi.path(path);
		Builder builder = target.request(MediaType.APPLICATION_JSON_TYPE);
		addAuthorization(builder, targetHost);
		Response response = builder.method(method);
		return response;
	}

	private <T> T getClass(WebTarget target, String method, Class<T> klass)
			throws IOException, InvalidUserLoginException, ResponseProcessingException {
		Builder builder = target.request(MediaType.APPLICATION_JSON_TYPE);
		addAuthorization(builder, targetHost);
		Response response = builder.method(method);
		if (response.getStatus() == Response.Status.OK.getStatusCode()) {
			InputStream in = (InputStream) response.getEntity();
			T instance = jsonMapper.readValue(in, klass);
			return instance;
		} else {
			throw new ResponseProcessingException(response, "could not process response from WebTarget: " + target);
		}
	}

	private void addAuthorization(Builder builder, String targetHost) throws InvalidUserLoginException {
		// Temporary implementation until we really tie in active directory.
		DesktopUser user = authService.getUser();

		authService.addAuthorizationTicket(builder, targetHost);

		if (user != null) {
			String username = user.getUsername();
			builder.header("X-Authorization", username);
		}
	}
}
