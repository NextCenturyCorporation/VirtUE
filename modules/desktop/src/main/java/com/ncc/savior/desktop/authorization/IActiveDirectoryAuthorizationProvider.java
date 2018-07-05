package com.ncc.savior.desktop.authorization;

import javax.ws.rs.client.Invocation.Builder;

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
	 * @throws InvalidUserLoginException
	 */
	DesktopUser getCurrentUser() throws InvalidUserLoginException;

	/**
	 * Attempt to login to a new user.
	 *
	 * @param domain
	 *            - can be null for no domain, but not implementations will support
	 *            that feature.
	 * @param username
	 * @param password
	 * @return
	 * @throws InvalidUserLoginException
	 */
	DesktopUser login(String domain, String username, String password) throws InvalidUserLoginException;

	/**
	 * Returns current token for single sign on.
	 *
	 * @return
	 * @throws InvalidUserLoginException
	 */
	byte[] getCurrentToken(String serverPrinc) throws InvalidUserLoginException;

	/**
	 * Attempt to logout of the current user. This is not guaranteed to log out
	 * particularly in cases where the user is logged into the OS itself.
	 */
	void logout();

	void addAuthorizationTicket(Builder builder, String targetHost) throws InvalidUserLoginException;
}
