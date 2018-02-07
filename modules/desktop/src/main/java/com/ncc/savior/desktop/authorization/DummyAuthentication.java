package com.ncc.savior.desktop.authorization;

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
	public byte[] getCurrentToken() {
		return null;
	}

	@Override
	public void logout() {
		// do nothing
	}

	@Override
	public String getAuthorizationTicket(String spn) {
		// TODO Auto-generated method stub
		return null;
	}

}
