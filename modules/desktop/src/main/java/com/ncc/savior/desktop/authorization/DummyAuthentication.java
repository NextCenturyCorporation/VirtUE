package com.ncc.savior.desktop.authorization;

import javax.ws.rs.client.Invocation.Builder;

public class DummyAuthentication implements IActiveDirectoryAuthorizationProvider {

	private DesktopUser currentUser;

	@Override
	public DesktopUser getCurrentUser() {
		return currentUser;
	}

	@Override
	public DesktopUser login(String domain, String username, String password) {
		currentUser = new DesktopUser(domain, username);
		return currentUser;
	}

	@Override
	public byte[] getCurrentToken(String principal) {
		return null;
	}

	@Override
	public void logout() {
		// do nothing
	}

	@Override
	public void addAuthorizationTicket(Builder builder, String targetHost) throws InvalidUserLoginException {
		// do nothing

	}
}
