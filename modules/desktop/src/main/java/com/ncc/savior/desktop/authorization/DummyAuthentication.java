package com.ncc.savior.desktop.authorization;

public class DummyAuthentication implements IActiveDirectoryAuthorizationProvider {

	@Override
	public DesktopUser getCurrentUser() {
		return null;
	}

	@Override
	public DesktopUser login(String domain, String username, String password) {
		return new DesktopUser(domain, username);
	}

	@Override
	public byte[] getCurrentToken() {
		return null;
	}

	@Override
	public void logout() {
		// do nothing
	}

}
