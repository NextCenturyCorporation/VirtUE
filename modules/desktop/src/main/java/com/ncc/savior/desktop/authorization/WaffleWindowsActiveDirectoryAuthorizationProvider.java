package com.ncc.savior.desktop.authorization;

import java.io.InputStream;
import java.util.Base64;

import javax.ws.rs.client.Invocation.Builder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.scene.image.Image;
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
	private static final String HEADER_AUTHORIZATION = "Authorization";
	private static final String DEFAULT_SECURITY_PACKAGE = "Negotiate";
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
		setCurrentImage(user);
		if (imp != null) {
			imp.revertToSelf();
		}
		return user;

	}

	private void setCurrentImage(DesktopUser user) {
		String uri = "/images/user/" + user.getUsername() + ".jpg";
		InputStream stream = DesktopUser.class.getResourceAsStream(uri);
		if (stream != null) {
			Image img = new Image(stream);
			user.setImage(img);
		}
	}

	@Override
	public DesktopUser login(String domain, String username, String password) {
		logout();
		impersonatedUser = auth.logonDomainUser(username, domain, password);
		IWindowsImpersonationContext imp = null;
		imp = impersonatedUser.impersonate();
		logger.debug("impersonating new user: " + WindowsAccountImpl.getCurrentUsername());
		DesktopUser user = new DesktopUser(domain, username);
		setCurrentImage(user);
		imp.revertToSelf();
		return user;
	}

	@Override
	public void logout() {
		if (impersonatedUser != null) {
			impersonatedUser.dispose();
			impersonatedUser = null;
		}
	}

	@Override
	public byte[] getCurrentToken(String serverPrinc) {
		try {
			IWindowsImpersonationContext imp = null;
			String current = WindowsAccountImpl.getCurrentUsername();
			if (impersonatedUser != null) {
				imp = impersonatedUser.impersonate();
				// impersonatedUser.get
			}
			IWindowsCredentialsHandle clientCredentials = WindowsCredentialsHandleImpl
					.getCurrent(DEFAULT_SECURITY_PACKAGE);
			clientCredentials.initialize();
			WindowsSecurityContextImpl clientContext = new WindowsSecurityContextImpl();
			clientContext.setPrincipalName(current);
			clientContext.setCredentialsHandle(clientCredentials);
			clientContext.setSecurityPackage(DEFAULT_SECURITY_PACKAGE);
			clientContext.initialize(null, null, serverPrinc);
			byte[] token = clientContext.getToken();
			if (imp != null) {
				imp.revertToSelf();
			}
			return token;
		} catch (Exception e) {
			logger.error("temp error", e);
			return null;
		}
	}

	private String getAuthorizationTicket(String targetHost) {
		if (null == targetHost || targetHost.trim().isEmpty()) {
			return null;
		}
		String serverPrinc = "HTTP/" + targetHost;
		byte[] token2 = WindowsSecurityContextImpl.getCurrent(DEFAULT_SECURITY_PACKAGE, "HTTP/" + targetHost)
				.getToken();
		byte[] token = getCurrentToken(serverPrinc);
		byte[] encoded = Base64.getEncoder().encode(token);
		String encodedStr = new String(encoded);
		return DEFAULT_SECURITY_PACKAGE + " " + encodedStr;
	}

	@Override
	public void addAuthorizationTicket(Builder builder, String targetHost) {
		String ticket = getAuthorizationTicket(targetHost);
		if (ticket != null) {
			builder.header(HEADER_AUTHORIZATION, ticket);
		}
	}

}
