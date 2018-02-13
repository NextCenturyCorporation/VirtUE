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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import waffle.windows.auth.impl.WindowsSecurityContextImpl;

public class KerberosClientTest {
	public static void main(String[] args) throws IOException {
		Client client = ClientBuilder.newClient();
		WebTarget t = client.target("http://ec2-54-208-224-247.compute-1.amazonaws.com:8080/api/");
		// WebTarget t = client.target("http://localhost:8080/api/");
		WebTarget t2 = t.path("desktop").path("virtue");
		Builder builder = t2.request(MediaType.APPLICATION_JSON_TYPE);
		// WebTarget t2 = t;
		// Builder builder = t2.request();
		addAuthorization(builder);
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

	private static void addAuthorization(Builder builder) {
		String authHeader = "Negotiate " + getKrbToken("HTTP/ec2-54-208-224-247.compute-1.amazonaws.com");
		builder.header("Authorization", authHeader);
	}

	public static final String securityPackage = "Negotiate";

	public static String getKrbToken(String aTargetSPName) {
		if (null == aTargetSPName || aTargetSPName.trim().isEmpty()) {
			return null;
		}
		byte[] token = WindowsSecurityContextImpl.getCurrent(securityPackage, aTargetSPName).getToken();
		byte[] encoded = Base64.getEncoder().encode(token);
		String encodedStr = new String(encoded);
		System.out.println(encodedStr);
		return encodedStr;
	}
}
