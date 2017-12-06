package com.ncc.savior.desktop.authorization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import waffle.windows.auth.IWindowsCredentialsHandle;
import waffle.windows.auth.IWindowsIdentity;
import waffle.windows.auth.IWindowsImpersonationContext;
import waffle.windows.auth.impl.WindowsAccountImpl;
import waffle.windows.auth.impl.WindowsAuthProviderImpl;
import waffle.windows.auth.impl.WindowsCredentialsHandleImpl;
import waffle.windows.auth.impl.WindowsSecurityContextImpl;

public class WaffleWindowsActiveDirectoryAuthorizationProvider implements IActiveDirectoryAuthorizationProvider {
	private static final Logger logger = LoggerFactory
			.getLogger(WaffleWindowsActiveDirectoryAuthorizationProvider.class);

	final String DEFAULT_SECURITY_PACKAGE = "Negotiate";
	private WindowsAuthProviderImpl auth;

	private IWindowsIdentity impersonatedUser;

	public WaffleWindowsActiveDirectoryAuthorizationProvider() {
		auth = new WindowsAuthProviderImpl();
	}

	@Override
	public DesktopUser getCurrentUser() {
		IWindowsImpersonationContext imp = null;
		if (impersonatedUser != null) {
			imp = impersonatedUser.impersonate();
		}
		String fqd = WindowsAccountImpl.getCurrentUsername();
		DesktopUser user = DesktopUser.fromFullyQualifiedDomainName(fqd);
		if (imp != null) {
			imp.revertToSelf();
		}
		return user;

	}

	@Override
	public DesktopUser login(String domain, String username, String password) {
		if (impersonatedUser != null) {
			impersonatedUser.dispose();
		}
		impersonatedUser = auth.logonDomainUser(username, domain, password);
		IWindowsImpersonationContext imp = null;
		imp = impersonatedUser.impersonate();
		logger.debug("impersonating new user: " + WindowsAccountImpl.getCurrentUsername());
		DesktopUser user = new DesktopUser(domain, username);
		imp.revertToSelf();
		return user;
	}

	@Override
	public void logout() {
		if (impersonatedUser!=null) {
			impersonatedUser.dispose();
			impersonatedUser = null;
		}
	}

	@Override
	public byte[] getCurrentToken() {
		IWindowsImpersonationContext imp = null;
		if (impersonatedUser != null) {
			imp = impersonatedUser.impersonate();
		}
		String current = WindowsAccountImpl.getCurrentUsername();
		IWindowsCredentialsHandle clientCredentials = WindowsCredentialsHandleImpl.getCurrent(DEFAULT_SECURITY_PACKAGE);
		clientCredentials.initialize();
		WindowsSecurityContextImpl clientContext = new WindowsSecurityContextImpl();
		clientContext.setPrincipalName(current);
		clientContext.setCredentialsHandle(clientCredentials);
		clientContext.setSecurityPackage(DEFAULT_SECURITY_PACKAGE);
		clientContext.initialize(null, null, current);
		byte[] token = clientContext.getToken();
		if (imp != null) {
			imp.revertToSelf();
		}
		return token;
	}

}
