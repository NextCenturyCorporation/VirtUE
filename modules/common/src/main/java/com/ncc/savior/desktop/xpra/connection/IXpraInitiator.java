/*
 * Copyright (C) 2019 Next Century Corporation
 * 
 * This file may be redistributed and/or modified under either the GPL
 * 2.0 or 3-Clause BSD license. In addition, the U.S. Government is
 * granted government purpose rights. For details, see the COPYRIGHT.TXT
 * file at the root of this project.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 * 
 * SPDX-License-Identifier: (GPL-2.0-only OR BSD-3-Clause)
 */
package com.ncc.savior.desktop.xpra.connection;

import java.io.IOException;
import java.util.Set;

/**
 * Used to control and get status of xpra servers as well as start applications
 * on a given Xpra display.
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
	public Set<Integer> getXpraServersWithRetries() throws IOException;

	/**
	 * Starts xpra server on a new display and returns that display.
	 *
	 * @param display
	 *
	 * @return
	 * @throws IOException
	 */
	public int startXpraServer(int display) throws IOException;

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

	public interface IXpraInitatorFactory {
		public IXpraInitiator getXpraInitiator(IConnectionParameters params);
	}
}
