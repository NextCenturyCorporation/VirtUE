package com.ncc.savior.desktop.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KerberosPasswordClientTest {
	private static final String JSESSION_HEADER = "JSESSIONID";
	private static final Logger logger = LoggerFactory.getLogger(KerberosPasswordClientTest.class);

	private Client client;
	private WebTarget baseTarget;
	private WebTarget loginTarget;
	private WebTarget logoutTarget;
	private boolean printResponses = false;


	public KerberosPasswordClientTest(String server, int port, boolean http) {
		client = ClientBuilder.newClient();
		client.property(ClientProperties.FOLLOW_REDIRECTS, Boolean.FALSE);
		String url = "http" + (http ? "" : "s") + "://" + server + ":" + port;
		baseTarget = client.target(url);
		loginTarget = baseTarget.path("login");
		logoutTarget = baseTarget.path("logout");
//		loginTarget.property(ClientProperties.FOLLOW_REDIRECTS, Boolean.FALSE);
	}

	public static void main(String[] args) throws IOException {
		String server = "localhost";
		int port = 8080;
		boolean http = true;
		KerberosPasswordClientTest client = new KerberosPasswordClientTest(server, port, http);
		String token = client.login(args[0], args[1]);
		client.getAsString("/", token);
		logger.info("virtues :" + client.getAsString("desktop/virtue", token));
		logger.info("logout :" + client.logout(token));
		logger.info("virtues :" + client.getAsString("desktop/virtue", token));
	}

	public String getAsString(String pathFromBase, String token) {
		Builder builder = baseTarget.path(pathFromBase).request();
		builder.cookie(JSESSION_HEADER, token);
		Response response = builder.get();
		String result;
		try {
			result = responseToString(response);
			return result;
		} catch (IOException e) {
			logger.error("Failed to read response", e);
		}
		return null;
	}

	public InputStream getAsInputStream(String pathFromBase, String token) {
		Builder builder = baseTarget.path(pathFromBase).request();
		builder.cookie(JSESSION_HEADER, token);
		Response response = builder.get();
		return (InputStream) response.getEntity();
	}

	public int logout(String token) {
		Builder builder = logoutTarget.request();
		builder.cookie(JSESSION_HEADER, token);
		Response response = builder.get();
		return response.getStatus();
	}

	public String login(String username, String password) {
		MultivaluedMap<String, String> formData = new MultivaluedHashMap<String, String>();
		formData.add("username", username);
		formData.add("password", password);
		Builder builder = loginTarget.request(MediaType.TEXT_HTML);
		// builder.header("referer", loginTarget.getUri().toString());
		Entity<Form> entity = Entity.form(formData);
		Response response = builder.post(entity);
		if (printResponses) {
			String result;
			try {
				result = responseToString(response);
				logger.debug("Response (" + response.getStatus() + "):\n" + result);
			} catch (IOException e) {
				logger.error("Failed to read response", e);
			}
		}

		// getJSessionIdFromSetCookieHeader(response);

		String jsessionid = getJSessionIdFromCookies(response);
		if (jsessionid != null) {
			jsessionid = getJSessionIdFromSetCookieHeader(response);
		}

		return jsessionid;
	}

	private String getJSessionIdFromCookies(Response response) {
		Map<String, NewCookie> cookies = response.getCookies();
		NewCookie jcookie = cookies.get(JSESSION_HEADER);
		if (jcookie != null) {
			return jcookie.getValue();
		}
		// for (Entry<String, NewCookie> entry : cookies.entrySet()) {
		// System.out.println(" Cookie: " + entry.getKey() + " - " +
		// entry.getValue().getValue());
		// }
		return null;
	}

	private String getJSessionIdFromSetCookieHeader(Response response) {
		String jsession = null;
		MultivaluedMap<String, Object> hs = response.getHeaders();
		String cookie = (String) hs.getFirst("Set-Cookie");
		if (cookie != null) {
			int x = cookie.indexOf(JSESSION_HEADER);
			jsession = cookie.substring(x + JSESSION_HEADER.length() + 1);
			jsession = jsession.substring(0, jsession.indexOf(";"));
		}
		return jsession;
	}

	private String responseToString(Response response) throws IOException {
		InputStream is = (InputStream) response.getEntity();
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder result = new StringBuilder();
		String line;
		boolean flag = false;
		String newLine = System.getProperty("line.separator");
		while ((line = reader.readLine()) != null) {
			result.append(flag ? newLine : "").append(line);
			flag = true;
		}
		return result.toString();
	}
}
