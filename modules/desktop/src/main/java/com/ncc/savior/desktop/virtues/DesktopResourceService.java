/*
 * Copyright (C) 2019 Next Century Corporation
 *
 * This file may be redistributed and/or modified under either the GPL
 * 2.0 or 3-Clause BSD license. In addition, the U.S. Government is
 * granted government purpose rights. For details, see the COPYRIGHT.TXT
 * file at the root of this project.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 *
 * SPDX-License-Identifier: (GPL-2.0-only OR BSD-3-Clause)
 */
package com.ncc.savior.desktop.virtues;

import java.awt.Image;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidParameterException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.imageio.ImageIO;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.NotAcceptableException;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ncc.savior.desktop.alerting.PlainAlertMessage;
import com.ncc.savior.desktop.alerting.UserAlertingServiceHolder;
import com.ncc.savior.desktop.authorization.AuthorizationService;
import com.ncc.savior.desktop.authorization.DesktopUser;
import com.ncc.savior.desktop.authorization.InvalidUserLoginException;
import com.ncc.savior.util.SaviorErrorCode;
import com.ncc.savior.util.SaviorException;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.ClipboardPermission;
import com.ncc.savior.virtueadmin.model.desktop.DesktopVirtue;
import com.ncc.savior.virtueadmin.model.desktop.DesktopVirtueApplication;

public class DesktopResourceService {
	private static final Logger logger = LoggerFactory.getLogger(DesktopResourceService.class);
	private static final String DEFAULT_ICON_KEY = "DEFAULT";
	private Client client;
	private ObjectMapper jsonMapper;
	private WebTarget baseApi;
	private AuthorizationService authService;
	private String targetHost;
	private BridgeSensorService bridgeSensorService;

	public DesktopResourceService(AuthorizationService authService, String baseApiUri, boolean allowAllHostnames,
			BridgeSensorService bridgeSensorService) {
		try {
			this.targetHost = new URI(baseApiUri).getHost();
		} catch (URISyntaxException e1) {
			String error = "Unable to get Subject Principal Name from baseUrl=" + baseApiUri;
			logger.error(error, e1);
			throw new InvalidParameterException(error);
		}
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
		this.bridgeSensorService = bridgeSensorService;
	}

	public DesktopResourceService(AuthorizationService authService, String baseApiUri,
			BridgeSensorService bridgeSensorService) {
		this(authService, baseApiUri, false, bridgeSensorService);
	}

	public List<DesktopVirtue> getVirtues() throws IOException, UserLoggedOutException {
		List<DesktopVirtue> instances;
		try {
			Response r = getListOfClass("virtue", "GET");
			InputStream in = (InputStream) r.getEntity();
			// logger.debug("Status code is: " + r.getStatus());
			if (r.getStatus() >= 400) {
				String data = streamToString(in);
				logger.error("response (" + r.getStatus() + "): " + data);
				PlainAlertMessage pam = new PlainAlertMessage("Server error message", "Logged out by server");
				UserAlertingServiceHolder.sendAlert(pam);
				throw new UserLoggedOutException();
			} else {
				instances = jsonMapper.readValue(in, new TypeReference<List<DesktopVirtue>>() {
				});
			}
		} catch (ProcessingException | NotAcceptableException e) {
			throw new IOException(e);
		}
		return instances;
	}

	public List<DesktopVirtue> getApplicationsWithTag(String tag) {
		List<DesktopVirtue> instances;
		try {
			Response r = getListOfClass("applications/" + tag, "GET");
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

			logger.error("error attempting to get virtues.", e);
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

	public DesktopVirtueApplication startApplication(DesktopVirtue virtue, ApplicationDefinition appDefn)
			throws IOException {
		WebTarget target = baseApi.path("virtue").path(virtue.getId()).path(appDefn.getId()).path("start");
		String params = appDefn.getParameters();
		if (params != null) {
			target = target.queryParam("cliParams", params);
		}
		DesktopVirtueApplication returnedApp = getClass(target, "GET", DesktopVirtueApplication.class);
		logger.debug("Started app=" + returnedApp);

		ApplicationBridgeSensorMessage messageObj = new ApplicationBridgeSensorMessage("Started Application",
				authService.getUser().getUsername(), MessageType.START_APPLICATION, virtue.getId(), virtue.getName(),
				appDefn.getId(), appDefn.getName());
		bridgeSensorService.sendMessage(messageObj);

		return returnedApp;

	}

	// public DesktopVirtueApplication startApplicationFromTemplate(String
	// templateId, ApplicationDefinition appDefn)
	// throws IOException {
	// WebTarget target =
	// baseApi.path("template").path(templateId).path(appDefn.getId()).path("start");
	// DesktopVirtueApplication returnedApp = getClass(target, "GET",
	// DesktopVirtueApplication.class);
	// logger.debug("Started app=" + returnedApp);
	// return returnedApp;
	// }

	public Collection<DesktopVirtueApplication> getReconnectionApps(String virtueId) {
		try {
			Response r = getListOfClass("virtue/" + virtueId + "/reconnect", "GET");
			InputStream in = (InputStream) r.getEntity();
			Collection<DesktopVirtueApplication> instances;
			if (r.getStatus() >= 400) {
				String data = streamToString(in);
				logger.error("response (" + r.getStatus() + "): " + data);
				instances = new ArrayList<DesktopVirtueApplication>();
			} else {
				instances = jsonMapper.readValue(in, new TypeReference<List<DesktopVirtueApplication>>() {
				});
			}
			return instances;
		} catch (InvalidUserLoginException e) {
			throw new SaviorException(SaviorErrorCode.USER_NOT_AUTHORIZED, "User not authorized to get reconnect apps",
					e);
		} catch (JsonParseException | JsonMappingException e) {
			throw new SaviorException(SaviorErrorCode.JSON_ERROR, "Error trying to parse reconnection apps", e);
		} catch (IOException e) {
			throw new SaviorException(SaviorErrorCode.UNKNOWN_ERROR, "Unknown error trying to get reconnection apps",
					e);
		}
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

	private Response getListOfClass(String path, String method) throws IOException, InvalidUserLoginException {
		WebTarget target = baseApi.path(path);
		Builder builder = target.request(MediaType.APPLICATION_JSON_TYPE);
		addAuthorization(builder, targetHost);
		Response response = builder.method(method);
		return response;
	}

	private <T> T getClass(WebTarget target, String method, Class<T> klass)
			throws IOException, InvalidUserLoginException {
		Builder builder = target.request(MediaType.APPLICATION_JSON_TYPE);
		addAuthorization(builder, targetHost);
		Response response = builder.method(method);
		InputStream in = (InputStream) response.getEntity();
		if (response.getStatus() == 200) {
			T instance = jsonMapper.readValue(in, klass);
			return instance;
		} else {
			String responseString = streamToString(in);
			String error = "Error in request to " + target.toString() + " Status:" + response.getStatus()
					+ " response: " + responseString;
			logger.error(error);
			throw new RuntimeException(error);
		}
	}

	private Image getImage(WebTarget target, String method) throws IOException, InvalidUserLoginException {
		Builder builder = target.request(MediaType.MEDIA_TYPE_WILDCARD);
		addAuthorization(builder, targetHost);
		Response response = builder.method(method);
		InputStream in = (InputStream) response.getEntity();
		Image image = ImageIO.read(in);
		return image;

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

	public DesktopVirtue createVirtue(String virtueTemplateId) throws IOException {
		// template/{templateId}/start
		WebTarget target = baseApi.path("template").path(virtueTemplateId).path("start");
		DesktopVirtue startingVirtue = getClass(target, "GET", DesktopVirtue.class);
		// logger.debug("Started app=" + returnedApp);
		VirtueBridgeSensorMessage messageObj = new VirtueBridgeSensorMessage("Created Virtue",
				authService.getUser().getUsername(), MessageType.CREATE_VIRTUE, startingVirtue.getId(),
				startingVirtue.getName());
		bridgeSensorService.sendMessage(messageObj);
		return startingVirtue;

	}

	public DesktopVirtue startVirtue(String virtueId) throws InvalidUserLoginException, IOException {
		WebTarget target = baseApi.path("virtue").path(virtueId).path("start");
		DesktopVirtue virtue = getClass(target, "GET", DesktopVirtue.class);
		if (logger.isTraceEnabled()) {
			logger.trace("Started virtue=" + virtue);
		}
		VirtueBridgeSensorMessage messageObj = new VirtueBridgeSensorMessage("Started Virtue",
				authService.getUser().getUsername(), MessageType.START_VIRTUE, virtueId, virtue.getName());
		bridgeSensorService.sendMessage(messageObj);
		return virtue;
	}

	public DesktopVirtue stopVirtue(String virtueId) throws InvalidUserLoginException, IOException {
		WebTarget target = baseApi.path("virtue").path(virtueId).path("stop");
		DesktopVirtue virtue = getClass(target, "GET", DesktopVirtue.class);
		if (logger.isTraceEnabled()) {
			logger.trace("Stopping virtue=" + virtue);
		}
		VirtueBridgeSensorMessage messageObj = new VirtueBridgeSensorMessage("Stopped Virtue",
				authService.getUser().getUsername(), MessageType.STOP_VIRTUE, virtueId, virtue.getName());
		bridgeSensorService.sendMessage(messageObj);
		return virtue;
	}

	public List<ClipboardPermission> getAllComputedPermissions() {
		try {
			Response r = getListOfClass("permissions", "GET");
			InputStream in = (InputStream) r.getEntity();
			List<ClipboardPermission> instances;
			if (r.getStatus() >= 400) {
				String data = streamToString(in);
				logger.error("response (" + r.getStatus() + "): " + data);
				instances = new ArrayList<ClipboardPermission>();
			} else {
				instances = jsonMapper.readValue(in, new TypeReference<List<ClipboardPermission>>() {
				});
			}
			return instances;
		} catch (InvalidUserLoginException e) {
			throw new SaviorException(SaviorErrorCode.USER_NOT_AUTHORIZED, "User not authorized to get permissions", e);
		} catch (JsonParseException | JsonMappingException e) {
			throw new SaviorException(SaviorErrorCode.JSON_ERROR, "Error trying to parse permissions", e);
		} catch (IOException e) {
			throw new SaviorException(SaviorErrorCode.UNKNOWN_ERROR, "Unknown error trying to get permissions", e);
		}
	}

	public DesktopVirtue terminateVirtue(String virtueId) throws InvalidUserLoginException, IOException {
		WebTarget target = baseApi.path("virtue").path(virtueId).path("terminate");
		DesktopVirtue virtue = getClass(target, "GET", DesktopVirtue.class);
		if (logger.isTraceEnabled()) {
			logger.trace("Stopping virtue=" + virtue);
		}
		VirtueBridgeSensorMessage messageObj = new VirtueBridgeSensorMessage("Terminated Virtue",
				authService.getUser().getUsername(), MessageType.TERMINATE_VIRTUE, virtueId, virtue.getName());
		bridgeSensorService.sendMessage(messageObj);
		return virtue;
	}

	public Image getIcon(String iconKey) throws InvalidUserLoginException, IOException {
		if (iconKey == null) {
			iconKey = DEFAULT_ICON_KEY;
		}
		WebTarget target = baseApi.path("icon").path(iconKey);
		Image img = getImage(target, "GET");
		if (logger.isTraceEnabled()) {
			logger.trace("Retrieving image");
		}
		return img;
	}
}
