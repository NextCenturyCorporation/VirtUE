package com.ncc.savior.desktop.authorization;

import java.util.Base64;

import waffle.windows.auth.IWindowsAuthProvider;
import waffle.windows.auth.IWindowsCredentialsHandle;
import waffle.windows.auth.IWindowsIdentity;
import waffle.windows.auth.impl.WindowsAccountImpl;
import waffle.windows.auth.impl.WindowsAuthProviderImpl;
import waffle.windows.auth.impl.WindowsCredentialsHandleImpl;
import waffle.windows.auth.impl.WindowsSecurityContextImpl;

public class WaffleGetToken {
	public static void main(String[] args) {
		try {
			System.out.println("Your windows login user is: " + WindowsAccountImpl.getCurrentUsername());
		} catch (Throwable t) {
			System.out.println("Attempted to get current windows login for fun, but failed.");
		}

		if (args.length != 3) {
			System.err.println("Invalid parameters.  \nProgram requires 3 parameters username, domain, and password");
		}
		try {
			final IWindowsAuthProvider auth = new WindowsAuthProviderImpl();
			final String securityPackage = "Negotiate";
			try {
			System.out.println("Attempting to login " + args[0] + "@" + args[1]);
				IWindowsIdentity id = auth.logonDomainUser(args[0], args[1], args[2]);
				id.impersonate();
			} catch (Throwable t) {
				System.err.println("Login failed, but continuing with current user");
			}
			// IWindowsImpersonationContext ip = id.impersonate();
			IWindowsCredentialsHandle clientCredentials = WindowsCredentialsHandleImpl.getCurrent(securityPackage);
			clientCredentials.initialize();
			WindowsSecurityContextImpl clientContext = new WindowsSecurityContextImpl();
			clientContext.setPrincipalName(WindowsAccountImpl.getCurrentUsername());
			clientContext.setCredentialsHandle(clientCredentials);
			clientContext.setSecurityPackage(securityPackage);
			clientContext.initialize(null, null, args[0]);
			byte[] token = clientContext.getToken();
			System.out.println("Your windows login user is: " + WindowsAccountImpl.getCurrentUsername());
			final String clientToken = Base64.getEncoder().encodeToString(token);
			System.out.println("Token (Base64): " + clientToken);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
}
