package com.ncc.savior.desktop.xpra.connection;

import java.io.IOException;
import java.util.Set;

/**
 * Used to control and get status of xpra servers.
 *
 */
public interface IXpraInitiator {

	/**
	 * Gets the list of displays which have an xpra server attached via 'xpra list'
	 * command.
	 *
	 * @return
	 * @throws IOException
	 */
	public Set<Integer> getXpraServers() throws IOException;

	/**
	 * Starts xpra server on a new display and returns that display.
	 *
	 * @return
	 * @throws IOException
	 */
	public int startXpraServer() throws IOException;

	/**
	 * Attempts to stop an xpra server on a certain display.
	 *
	 * @param display
	 * @return
	 * @throws IOException
	 */
	public boolean stopXpraServer(int display) throws IOException;

	/**
	 * Attempts to stop all xpra servers.
	 *
	 * @throws IOException
	 */
	public void stopAllXpraServers() throws IOException;

	/**
	 * Starts a new application on the given display.
	 *
	 * @param display
	 * @param command
	 * @throws IOException
	 */
	public void startXpraApp(int display, String command) throws IOException;

}
