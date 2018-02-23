package com.ncc.savior.desktop.authorization;

import javax.ws.rs.client.Invocation.Builder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.virtueadmin.model.OS;

public class AuthorizationService {
	private static final Logger logger = LoggerFactory.getLogger(AuthorizationService.class);
	private static final String PROPERTY_OS_NAME = "os.name";
	private static final String OS_STRING_AIX = "aix";
	private static final String OS_STRING_LINUX = "nux";
	private static final String OS_STRING_UNIX = "nix";
	private static final String OS_STRING_WINDOWS = "win";
	private static final String OS_STRING_MAC = "mac";
	private static final String OS_STRING_SOLARIS = "sunos";

	private OS os;
	private IActiveDirectoryAuthorizationProvider authProvider;
	private String requiredDomain;
	private boolean dummySecurity;
	private String loginUrl;
	private String logoutUrl;

	public AuthorizationService(String requiredDomain, boolean dummySecurity, String loginUrl, String logoutUrl) {
		this.requiredDomain = requiredDomain;
		if (this.requiredDomain != null && this.requiredDomain.equals("")) {
			this.requiredDomain = null;
		}
		this.dummySecurity = dummySecurity;
		this.os = getOs();
		this.loginUrl = loginUrl;
		this.logoutUrl = logoutUrl;
		createAuthProviderChain();
	}

	public AuthorizationService(String loginUrl, String logoutUrl) {
		this(null, false, loginUrl, logoutUrl);
	}

	private void createAuthProviderChain() {
		if (dummySecurity) {
			authProvider = new DummyAuthentication();
		}

		switch (os) {
		case WINDOWS:
			authProvider = new WaffleWindowsActiveDirectoryAuthorizationProvider();
			break;
		case MAC:
		case LINUX:
			// return new UsernamePasswordKerberosAuthorizationService(loginUrl, logoutUrl);
			authProvider = new JavaGssActiveDirectoryAuthorizationProvider();
			break;
		}
		DesktopUser user = null;
		try {
			user = authProvider.getCurrentUser();
		} catch (InvalidUserLoginException e) {
			logger.warn(
					"Error testing Single-Sign-On authentication provider.  Falling back to username/password authentication.",
					e);
			authProvider = new UsernamePasswordKerberosAuthorizationService(loginUrl, logoutUrl);
			return;
		} catch (RuntimeException e) {
			logger.warn(
					"Error testing Single-Sign-On authentication provider.  Falling back to username/password authentication.",
					e);
			authProvider = new UsernamePasswordKerberosAuthorizationService(loginUrl, logoutUrl);
			return;
		}
		if (requiredDomain == null || user == null
				|| !requiredDomain.toUpperCase().equals(user.getDomain().toUpperCase())) {
			authProvider = new UsernamePasswordKerberosAuthorizationService(loginUrl, logoutUrl);
		}
	}

	private OS getOs() {
		String prop = System.getProperty(PROPERTY_OS_NAME);
		if (prop == null) {
			throw new RuntimeException("Unable to find OS from " + PROPERTY_OS_NAME);
		}
		prop = prop.toLowerCase();
		if (prop.indexOf(OS_STRING_WINDOWS) >= 0) {
			return OS.WINDOWS;
		} else if (prop.indexOf(OS_STRING_MAC) >= 0) {
			return OS.MAC;
		} else if (prop.indexOf(OS_STRING_SOLARIS) >= 0) {
			throw new RuntimeException("Solaris not supported!");
		} else if (prop.indexOf(OS_STRING_UNIX) >= 0 || prop.indexOf(OS_STRING_LINUX) >= 0
				|| prop.indexOf(OS_STRING_AIX) > 0) {
			return OS.LINUX;
		}
		throw new RuntimeException("Unsupported OS string=" + prop);
	}

	public DesktopUser getUser() throws InvalidUserLoginException {
		try {
			DesktopUser user = authProvider.getCurrentUser();
			return user;
		} catch (InvalidUserLoginException e) {
			return null;
		}
	}

	public DesktopUser login(String domain, String username, String password) {
		if (!dummySecurity && !(authProvider instanceof UsernamePasswordKerberosAuthorizationService)) {
			authProvider = new UsernamePasswordKerberosAuthorizationService(loginUrl, logoutUrl);
			// if (requiredDomain == null || requiredDomain.equals(domain)) {
			// return authProvider.login(domain, username, password);
			// } else {
			// String msg = "Cannot login. Domain (" + domain + ") is not the required
			// domain (" + requiredDomain
			// + ")";
			// throw new RuntimeException(msg);
			// }
		}
		return authProvider.login(domain, username, password);
	}

	public void logout() {
		authProvider.logout();
	}

	public String getRequiredDomain() {
		return requiredDomain;
	}

	public void addAuthorizationTicket(Builder builder, String targetHost) throws InvalidUserLoginException {
		authProvider.addAuthorizationTicket(builder, targetHost);
	}

}
