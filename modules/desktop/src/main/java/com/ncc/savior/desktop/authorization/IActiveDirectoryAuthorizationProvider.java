package com.ncc.savior.desktop.authorization;

public interface IActiveDirectoryAuthorizationProvider {

	DesktopUser getCurrentUsername();

	DesktopUser login(String domain, String username, String password);

	byte[] getNewToken();

}
