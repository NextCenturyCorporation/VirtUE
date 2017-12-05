package com.ncc.savior.desktop.authorization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import waffle.windows.auth.IWindowsCredentialsHandle;
import waffle.windows.auth.IWindowsIdentity;
import waffle.windows.auth.impl.WindowsAccountImpl;
import waffle.windows.auth.impl.WindowsAuthProviderImpl;
import waffle.windows.auth.impl.WindowsCredentialsHandleImpl;
import waffle.windows.auth.impl.WindowsSecurityContextImpl;

public class WaffleWindowsActiveDirectoryAuthorizationProvider implements IActiveDirectoryAuthorizationProvider {
	private static final Logger logger = LoggerFactory
			.getLogger(WaffleWindowsActiveDirectoryAuthorizationProvider.class);

	final String DEFAULT_SECURITY_PACKAGE = "Negotiate";
	private WindowsAuthProviderImpl auth;

	private String current;

	public WaffleWindowsActiveDirectoryAuthorizationProvider() {
		auth = new WindowsAuthProviderImpl();
		current = WindowsAccountImpl.getCurrentUsername();
	}

	@Override
	public DesktopUser getCurrentUsername() {
		String fqd = current;
		return DesktopUser.fromFullyQualifiedDomainName(fqd);
	}

	@Override
	public DesktopUser login(String domain, String username, String password) {
		IWindowsIdentity id = auth.logonDomainUser(username, domain, password);
		current = id.getFqn();
		id.impersonate();
		logger.debug("impersonating new user" + WindowsAccountImpl.getCurrentUsername());
		return new DesktopUser(domain, username);
	}

	@Override
	public byte[] getNewToken() {
		IWindowsCredentialsHandle clientCredentials = WindowsCredentialsHandleImpl.getCurrent(DEFAULT_SECURITY_PACKAGE);
		clientCredentials.initialize();
		WindowsSecurityContextImpl clientContext = new WindowsSecurityContextImpl();
		clientContext.setPrincipalName(current);
		clientContext.setCredentialsHandle(clientCredentials);
		clientContext.setSecurityPackage(DEFAULT_SECURITY_PACKAGE);
		clientContext.initialize(null, null, current);
		byte[] token = clientContext.getToken();
		return token;
	}

}
