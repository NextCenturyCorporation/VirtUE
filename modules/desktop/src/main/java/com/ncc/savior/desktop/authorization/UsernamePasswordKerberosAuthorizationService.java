package com.ncc.savior.desktop.authorization;

import java.util.Date;
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

public class UsernamePasswordKerberosAuthorizationService implements IActiveDirectoryAuthorizationProvider {
	private static final Logger logger = LoggerFactory.getLogger(UsernamePasswordKerberosAuthorizationService.class);
	private static final String JSESSION_HEADER = "JSESSIONID";
	private Client client;
	private WebTarget loginTarget;
	private WebTarget logoutTarget;
	private DesktopUser currentUser;
	private long expireMillis;
	private String token;

	public UsernamePasswordKerberosAuthorizationService(String loginUrl, String logoutUrl) {
		client = ClientBuilder.newClient();
		client.property(ClientProperties.FOLLOW_REDIRECTS, Boolean.FALSE);
		loginTarget = client.target(loginUrl);
		logoutTarget = client.target(logoutUrl);
	}

	@Override
	public DesktopUser getCurrentUser() throws InvalidUserLoginException {
		verifyUserAndToken();
		return currentUser;
	}

	private synchronized void verifyUserAndToken() throws InvalidUserLoginException {
		long now = new Date().getTime();
		if (currentUser == null || token == null) {
			throw new InvalidUserLoginException("No user or token");
		}
		if (now > expireMillis) {
			throw new InvalidUserLoginException("Login has expired");
		}

	}

	@Override
	public synchronized DesktopUser login(String domain, String username, String password)
			throws InvalidUserLoginException {
		if (currentUser != null) {
			logout();
		}
		MultivaluedMap<String, String> formData = new MultivaluedHashMap<String, String>();
		formData.add("username", username);
		formData.add("password", password);
		// loginTarget.property(ClientProperties.FOLLOW_REDIRECTS, Boolean.TRUE);
		Builder builder = loginTarget.request(MediaType.TEXT_HTML);
		Entity<Form> entity = Entity.form(formData);
		Response response = builder.post(entity);
		// if (printResponses) {
		// String result;
		// try {
		// result = responseToString(response);
		// logger.debug("Response (" + response.getStatus() + "):\n" + result);
		// } catch (IOException e) {
		// logger.error("Failed to read response", e);
		// }
		// }

		Map<String, NewCookie> cookies = response.getCookies();
		MultivaluedMap<String, Object> headers = response.getHeaders();
		if (response.getStatus() >= 400) {
			throw new InvalidUserLoginException("login failed");
		} else if (response.getStatus() == 302) {
			String location = (String) headers.getFirst("Location");
			if (location.contains("error")) {
				throw new InvalidUserLoginException("login failed");
			}
		}
		NewCookie jcookie = cookies.get(JSESSION_HEADER);
		if (jcookie != null) {
			token = jcookie.getValue();
			Date expiry = jcookie.getExpiry();
			if (expiry != null) {
				expireMillis = expiry.getTime();
			} else {
				expireMillis = new Date().getTime() + 24 * 60 * 60 * 1000;
			}
		}
		currentUser = new DesktopUser(domain, username);
		return currentUser;
	}

	@Override
	public byte[] getCurrentToken(String serverPrinc) throws InvalidUserLoginException {
		verifyUserAndToken();
		return token.getBytes();
	}

	@Override
	public void logout() {
		Builder builder = logoutTarget.request();
		builder.cookie(JSESSION_HEADER, token);
		Response response = builder.get();
		if (logger.isTraceEnabled()) {
			logger.trace("logout response: " + response.getStatus());
		}
		currentUser = null;
	}

	@Override
	public void addAuthorizationTicket(Builder builder, String targetHost) throws InvalidUserLoginException {
		verifyUserAndToken();
		builder.cookie(JSESSION_HEADER, token);
	}
}
