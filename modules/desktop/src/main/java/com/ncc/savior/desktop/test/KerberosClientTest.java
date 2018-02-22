package com.ncc.savior.desktop.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Base64;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import com.sun.jna.platform.win32.Sspi;

import waffle.windows.auth.IWindowsCredentialsHandle;
import waffle.windows.auth.IWindowsIdentity;
import waffle.windows.auth.IWindowsImpersonationContext;
import waffle.windows.auth.impl.WindowsAccountImpl;
import waffle.windows.auth.impl.WindowsAuthProviderImpl;
import waffle.windows.auth.impl.WindowsCredentialsHandleImpl;
import waffle.windows.auth.impl.WindowsSecurityContextImpl;

public class KerberosClientTest {
	public static final String securityPackage = "Negotiate";

	public static void main(String[] args) throws IOException {
		String user = null;
		String password = null;
		String server = args[0];
		if (args.length > 2) {
			user = args[1];
			password = args[2];
		}
		Client client = ClientBuilder.newClient();
		WebTarget t = client.target("http://" + server + ":8080/");
		// WebTarget t = client.target("http://localhost:8080/api/");
		WebTarget t2 = t;

		String servicePrincipal = "HTTP/" + server;
		String token3 = getToken3(servicePrincipal, user, password);
		String token1 = getToken1(servicePrincipal);
		String token2 = getToken2(servicePrincipal);

		test(t2, token3);
		test(t2, token1);
		test(t2, token2);

	}

	private static void test(WebTarget t2, String token) throws IOException {
		Builder builder = t2.request();
		// WebTarget t2 = t;
		// Builder builder = t2.request();
		addAuthorization(builder, token);
		Response response = builder.method("GET");
		System.out.println(response.getStatus());
		InputStream entity = (InputStream) response.getEntity();
		BufferedReader reader = new BufferedReader(new InputStreamReader(entity));
		StringBuilder result = new StringBuilder();
		String line;
		boolean flag = false;
		String newLine = System.getProperty("line.separator");
		while ((line = reader.readLine()) != null) {
			result.append(flag ? newLine : "").append(line);
			flag = true;
		}
		System.out.println(result.toString());
	}

	private static void addAuthorization(Builder builder, String token) {
		String authHeader = "Negotiate " + token;
		builder.header("Authorization", authHeader);
	}

	private static String getToken1(String aTargetSPName) {
		byte[] b = WindowsSecurityContextImpl.getCurrent(securityPackage, aTargetSPName).getToken();
		String token = Base64.getEncoder().encodeToString(b);
		System.out.println("Token1=" + token);
		return token;
	}

	private static String getToken2(String serverPrincipal) {
		IWindowsImpersonationContext imp = null;
		String current = WindowsAccountImpl.getCurrentUsername();
		IWindowsCredentialsHandle clientCredentials = WindowsCredentialsHandleImpl.getCurrent(securityPackage);
		clientCredentials.initialize();
		WindowsSecurityContextImpl clientContext = new WindowsSecurityContextImpl();
		clientContext.setPrincipalName(current);
		clientContext.setCredentialsHandle(clientCredentials);
		clientContext.setSecurityPackage(securityPackage);
		clientContext.initialize(null, null, serverPrincipal);
		byte[] b = clientContext.getToken();
		if (imp != null) {
			imp.revertToSelf();
		}
		String token = Base64.getEncoder().encodeToString(b);
		System.out.println("Token2=" + token);
		return token;
	}

	private static String getToken3(String serverPrincipal, String username, String password) {
		IWindowsImpersonationContext imp = null;
		try {
			WindowsAuthProviderImpl auth = new WindowsAuthProviderImpl();

			IWindowsIdentity id = auth.logonDomainUser(username, "vrtu", password);
			// IWindowsIdentity id = auth.logonUser(username, password);
			String fqn = id.getFqn();

			IWindowsCredentialsHandle clientCredentials = new WindowsCredentialsHandleImpl(fqn,
					Sspi.SECPKG_CRED_OUTBOUND, securityPackage);
			clientCredentials.initialize();
			WindowsSecurityContextImpl clientContext = new WindowsSecurityContextImpl();
			clientContext.setPrincipalName(fqn);
			clientContext.setCredentialsHandle(clientCredentials);
			clientContext.setSecurityPackage(securityPackage);
			clientContext.initialize(null, null, serverPrincipal);
			byte[] b = clientContext.getToken();

			String token = Base64.getEncoder().encodeToString(b);
			System.out.println("Token3=" + token);
			return token;
		} finally {
			if (imp != null) {
				imp.revertToSelf();
			}
		}
	}
}