package com.ncc.savior.desktop.authorization;

/**
 * Provides authorization services for the application. Implementations are
 * often platform specific.
 *
 *
 */
public interface IActiveDirectoryAuthorizationProvider {

	/**
	 * Returns the currently logged in user for the application. If possible, check
	 * the OS to see if we can use the OS's logged in user.
	 *
	 * @return
	 */
	DesktopUser getCurrentUser();

	/**
	 * Attempt to login to a new user.
	 *
	 * @param domain
	 *            - can be null for no domain, but not implementations will support
	 *            that feature.
	 * @param username
	 * @param password
	 * @return
	 */
	DesktopUser login(String domain, String username, String password);

	/**
	 * Returns current token for single sign on.
	 *
	 * @return
	 */
	byte[] getCurrentToken();

}
