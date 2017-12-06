package com.ncc.savior.desktop.authorization;

import com.ncc.savior.virtueadmin.model.OS;

public class AuthorizationService {
	private static final String PROPERTY_OS_NAME = "os.name";
	private static final String OS_STRING_AIX = "aix";
	private static final String OS_STRING_LINUX = "nux";
	private static final String OS_STRING_UNIX = "nix";
	private static final String OS_STRING_WINDOWS = "win";
	private static final String OS_STRING_MAC = "mac";
	private static final String OS_STRING_SOLARIS = "sunos";

	private OS os;
	private IActiveDirectoryAuthorizationProvider authProvider;

	public AuthorizationService() {
		this.os = getOs();
		this.authProvider = createAuthProvider();
	}

	private IActiveDirectoryAuthorizationProvider createAuthProvider() {
		switch (os) {
		case WINDOWS:
			return new WaffleWindowsActiveDirectoryAuthorizationProvider();
		case MAC:
		case LINUX:
			return new JavaGssActiveDirectoryAuthorizationProvider();
		}
		// This should be unreachable since we set the OS first and that will throw if
		// the OS is indeterminable.
		throw new RuntimeException("Cannot find an IActiveDirectoryAuthorizationProvider for OS=" + os.toString());
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

	public DesktopUser getUser() {
		return authProvider.getCurrentUser();
	}

	public DesktopUser login(String domain, String username, String password) {
		return authProvider.login(domain, username, password);
	}

	public void logout() {
		authProvider.logout();
	}

}
